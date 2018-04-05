package com.xeomar.annex;

import com.xeomar.product.Product;
import com.xeomar.product.ProductBundle;
import com.xeomar.product.ProductCard;
import com.xeomar.util.LogUtil;
import com.xeomar.util.OperatingSystem;
import org.slf4j.Logger;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class Program implements Product {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private ProductCard card;

	private ProductBundle resourceBundle;

	private Path programDataFolder;

	private String title;

	private Process elevatedProcess;

	public Program() {
		try {
			this.card = new ProductCard().init( getClass() );
		} catch( IOException exception ) {
			throw new RuntimeException( "Error loading product card", exception );
		}
		this.resourceBundle = new ProductBundle( getClass() );
		this.programDataFolder = OperatingSystem.getUserProgramDataFolder( card.getArtifact(), card.getName() );
		this.title = card.getName();
	}

	public static void main( String[] commands ) {
		try {
			new Program().run( commands );
		} catch( Throwable throwable ) {
			throwable.printStackTrace( System.err );
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
		printHeader( card );

		log.debug( "Parsing commands..." );

		for( int index = 0; index < commands.length; index++ ) {
			String command = commands[ index ];
			switch( command ) {
				case UpdateFlag.TITLE: {
					title = commands[ index + 1 ];
					break;
				}
				case UpdateFlag.STREAM: {
					runTasksFromStdIn();
					break;
				}
				case UpdateFlag.FILE: {
					runTasksFromFile( new File( commands[ index + 1 ] ) );
					break;
				}
			}
		}
	}

	private void runTasksFromStdIn() throws IOException {
		runTasksFromStream( System.in, System.out );
	}

	private void runTasksFromFile( File file ) throws IOException {
		runTasksFromStream( new FileInputStream( file ), new ByteArrayOutputStream() );
	}

	private void runTasksFromStream( InputStream input, OutputStream output ) throws IOException {
		runTasksFromReader( new InputStreamReader( input, "utf-8" ), new OutputStreamWriter( output, "utf-8" ) );
	}

	public String runTasksFromString( String commands ) throws IOException {
		StringReader reader = new StringReader( commands );
		StringWriter writer = new StringWriter();
		runTasksFromReader( reader, writer );
		return writer.toString().trim();
	}

	public void runTasksFromReader( Reader reader, Writer writer ) throws IOException {
		BufferedReader buffer = new BufferedReader( reader );
		PrintWriter printWriter = new PrintWriter( writer );

		String line = buffer.readLine();
		while( line != null ) {
			AnnexTask task = parseTask( line );
			TaskResult result = executeTask( task );
			printWriter.print( result );
			//printWriter.print("\r\n")
			printWriter.print( "\n" );
			printWriter.flush();

			line = buffer.readLine();
		}
	}

	private void printHeader( ProductCard card ) {
		// These use System.err because System.in is used for communication
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

		TaskResult result = null;

		try {
			if( task.needsElevation() ) {
				if( elevatedProcess == null ) {
					// TODO Create elevated updater
					ProcessBuilder processBuilder = new ProcessBuilder();
					elevatedProcess = OperatingSystem.startProcessElevated( title, processBuilder );
				}

				if( elevatedProcess == null ) throw new RuntimeException( "Unable to create elevated process" );

				new BufferedWriter( new OutputStreamWriter( elevatedProcess.getOutputStream(), "utf-8" ) ).write( task.execute().toString() );
				result = TaskResult.parse( new BufferedReader( new InputStreamReader( elevatedProcess.getInputStream() ) ).readLine() );
			} else {
				result = task.execute();
			}
		} catch( Exception exception ) {
			String message = String.format( "%s: %s", exception.getClass().getSimpleName(), exception.getMessage() );
			result = new TaskResult( TaskStatus.FAILURE, message );
		}

		return result;
	}

	private AnnexTask parseTask( String line ) {
		String[] parameters = line.split( " " );
		String command = parameters[ 0 ];
		List<String> parameterList = Arrays.asList( Arrays.copyOfRange( parameters, 1, parameters.length ) );

		switch( command ) {
			case UpdateFlag.LAUNCH: {
				return new LaunchTask( parameterList );
			}
			case UpdateFlag.PAUSE: {
				return new PauseTask( parameterList );
			}
			case UpdateFlag.UPDATE: {
				return new UpdateTask( parameterList );
			}
			default: {
				throw new IllegalArgumentException( "Unknown command: " + command );
			}
		}
	}

}
