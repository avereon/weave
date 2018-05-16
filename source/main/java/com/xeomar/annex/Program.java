package com.xeomar.annex;

import com.xeomar.annex.task.*;
import com.xeomar.product.Product;
import com.xeomar.product.ProductBundle;
import com.xeomar.product.ProductCard;
import com.xeomar.util.*;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Program implements Product {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private ProductCard card;

	private ProductBundle resourceBundle;

	private Path programDataFolder;

	private String title;

	private Process elevatedProcess;

	private Parameters parameters;

	public Program() {
		try {
			this.card = new ProductCard().init( getClass() );
		} catch( IOException exception ) {
			throw new RuntimeException( "Error loading product card", exception );
		}
		this.title = card.getName();
		this.resourceBundle = new ProductBundle( getClass() );
		this.programDataFolder = OperatingSystem.getUserProgramDataFolder( card.getArtifact(), card.getName() );
	}

	public static void main( String[] commands ) {
		try {
			new Program().run( commands );
		} catch( Throwable throwable ) {
			throwable.printStackTrace( System.err );
			log.error( "Execution error", throwable );
		}
	}

	@Override
	public ProductCard getCard() {
		return card;
	}

	@Override
	public ClassLoader getClassLoader() {
		return super.getClass().getClassLoader();
	}

	@Override
	public ProductBundle getResourceBundle() {
		return resourceBundle;
	}

	@Override
	public Path getDataFolder() {
		return programDataFolder;
	}

	public void run( String[] commands ) throws IOException {
		// Parse parameters
		parameters = Parameters.parse( commands );

		// Configure logging
		LogUtil.configureLogging( this, parameters );

		// Print the program header
		printHeader( card );

		log.info( card.getName() + " started" );

		if( parameters.isSet( UpdateFlag.TITLE ) ) title = parameters.get( UpdateFlag.TITLE );

		boolean stream = parameters.isSet( UpdateFlag.STREAM );
		boolean file = parameters.isSet( UpdateFlag.FILE );

		if( stream & file ) {
			log.error( "Cannot use both stream and file parameters at the same time" );
			return;
		} else if( !(stream | file) ) {
			log.error( "Must use either stream or file to provide update commands" );
		}

		if( file ) runTasksFromFile( new File( parameters.get( UpdateFlag.FILE ) ) );
		if( stream ) runTasksFromStdIn();

		log.info( card.getName() + " finished" );
	}

	private List<TaskResult> runTasksFromStdIn() throws IOException {
		return runTasksFromStream( System.in, System.out );
	}

	private List<TaskResult> runTasksFromFile( File file ) throws IOException {
		return runTasksFromStream( new FileInputStream( file ), new ByteArrayOutputStream() );
	}

	private List<TaskResult> runTasksFromStream( InputStream input, OutputStream output ) throws IOException {
		return runTasksFromReader( new InputStreamReader( input, "utf-8" ), new OutputStreamWriter( output, "utf-8" ) );
	}

	public List<TaskResult> runTasksFromString( String commands ) throws IOException {
		StringReader reader = new StringReader( commands );
		StringWriter writer = new StringWriter();
		return runTasksFromReader( reader, writer );
	}

	public List<TaskResult> runTasksFromReader( Reader reader, Writer writer ) throws IOException {
		BufferedReader buffer = new BufferedReader( reader );
		PrintWriter printWriter = new PrintWriter( writer );

		List<TaskResult> results = new ArrayList<>();
		String line = buffer.readLine().trim();
		while( !TextUtil.isEmpty( line ) ) {
			AnnexTask task = parseTask( line );
			TaskResult result = executeTask( task );
			log.info( result.toString() );

			results.add( result );

			printWriter.print( result );
			printWriter.print( "\n" );
			printWriter.flush();

			if( result.getStatus() == TaskStatus.FAILURE ) break;

			line = buffer.readLine();
		}
		printWriter.close();

		// Closing the elevated process output stream should cause it to exit
		if( elevatedProcess != null ) elevatedProcess.getOutputStream().close();

		return results;
	}

	private void printHeader( ProductCard card ) {
		// These use System.err because System.out is used for communication
		System.err.println( card.getName() + " " + card.getVersion() );
		System.err.println( "Java " + System.getProperty( "java.runtime.version" ) );
	}

	private TaskResult executeTask( AnnexTask task ) {
		// Now for the hard part, figuring out how to execute the tasks.
		// The reason this is hard is because some of the update commands will
		// require elevated privileges. But we don't want to execute programs
		// with an elevated process. If this process is elevated, we need to take
		// care not to become a security issue by allowing others to execute
		// elevated processes through the program.
		//
		// That means that executing the tasks in sequence may be challenging if
		// there are some that need to be elevated and some that should not. If
		// the tasks all need to be elevated, or all do not need to be elevated,
		// the task execution is pretty straight forward. If they need to be mixed
		// then it is not trivial.
		//
		// If all the tasks do not need elevation then all the tasks can be
		// executed in this process, otherwise an elevated process will need to be
		// started and some tasks executed on the elevated process. This will also
		// cover the situation where all tasks need to be elevated.

		// If needsElevation is true then a separate, elevated, process will need
		// to be started to execute some tasks.

		TaskResult result;

		try {
			// Validate the task parameters before asking if it needs elevation
			task.validate();

			if( task.needsElevation() ) {
				if( elevatedProcess == null ) {
					ProcessBuilder processBuilder = new ProcessBuilder( ProcessCommands.forModule() );

					processBuilder.command().add( UpdateFlag.STREAM );

					if( parameters.isSet( LogFlag.LOG_FILE ) ) {
						processBuilder.command().add( LogFlag.LOG_FILE );
						processBuilder.command().add( parameters.get( LogFlag.LOG_FILE ).replace( ".log", "-elevated.log" ) );
					}
					if( parameters.isSet( LogFlag.LOG_LEVEL ) ) {
						processBuilder.command().add( LogFlag.LOG_LEVEL );
						processBuilder.command().add( parameters.get( LogFlag.LOG_LEVEL ) );
					}

//					File home = new File( System.getProperty( "user.home" ));
//					File logFile = new File( parameters.get( LogFlag.LOG_FILE ).replace( "%h", home.toString() ).replace( ".log", "-mvs.log" ) );
//					log.info( "MVS log file: " + logFile );
//					processBuilder.redirectOutput( ProcessBuilder.Redirect.to( logFile ) ).redirectError( ProcessBuilder.Redirect.to( logFile ) );

					//processBuilder.redirectError( ProcessBuilder.Redirect.INHERIT );
					log.info( "Elevated commands: " + TextUtil.toString( processBuilder.command(), " " ) );
					elevatedProcess = OperatingSystem.startProcessElevated( title, processBuilder );
				}

				log.trace( "Sending task commands to elevated process...");
				log.trace( "  commands: " + task.getOriginalLine() );

				elevatedProcess.getOutputStream().write( task.getOriginalLine().getBytes( TextUtil.CHARSET ) );
				elevatedProcess.getOutputStream().write( '\n' );
				elevatedProcess.getOutputStream().flush();

				log.trace( "Reading task result from elevated process...");
				result = TaskResult.parse( task, new BufferedReader( new InputStreamReader( elevatedProcess.getInputStream() ) ).readLine() );
				log.trace( "  result: " + result );
			} else {
				result = task.execute();
			}
		} catch( Exception exception ) {
			String message = String.format( "%s: %s", exception.getClass().getSimpleName(), exception.getMessage() );
			result = new TaskResult( task, TaskStatus.FAILURE, message );
		}

		return result;
	}

	private AnnexTask parseTask( String line ) {
		List<String> commands = TextUtil.split( line );
		String command = commands.get( 0 );
		List<String> parameterList = commands.subList( 1, commands.size() );

		AnnexTask task = null;
		switch( command ) {
			case UpdateTask.DELETE: {
				task = new DeleteTask( parameterList );
				break;
			}
			case UpdateTask.ECHO: {
				task = new EchoTask( parameterList );
				break;
			}
			case UpdateTask.LAUNCH: {
				task = new LaunchTask( parameterList );
				break;
			}
			case UpdateTask.MOVE: {
				task = new MoveTask( parameterList );
				break;
			}
			case UpdateTask.PAUSE: {
				task = new PauseTask( parameterList );
				break;
			}
			case UpdateTask.UNPACK: {
				task = new UnpackTask( parameterList );
				break;
			}
			default: {
				throw new IllegalArgumentException( "Unknown command: " + command );
			}
		}

		task.setOriginalLine( line );

		return task;
	}

}
