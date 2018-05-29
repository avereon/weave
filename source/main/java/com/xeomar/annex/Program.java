package com.xeomar.annex;

import com.xeomar.annex.task.*;
import com.xeomar.product.Product;
import com.xeomar.product.ProductBundle;
import com.xeomar.product.ProductCard;
import com.xeomar.util.LogUtil;
import com.xeomar.util.OperatingSystem;
import com.xeomar.util.Parameters;
import com.xeomar.util.TextUtil;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.slf4j.Logger;

import javax.net.SocketFactory;
import java.io.*;
import java.lang.invoke.MethodHandles;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Program implements Product {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private ProductCard card;

	private ProductBundle resourceBundle;

	private Path programDataFolder;

	private String title;

	private ElevatedHandler elevatedHandler;

	private Parameters parameters;

	private Alert alert;

	private boolean cancelled;

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

	public Parameters getParameters() {
		return parameters;
	}

	public String getTitle() {
		return title;
	}

	public void run( String[] commands ) throws Exception {
		// Parse parameters
		parameters = Parameters.parse( commands );

		// Configure logging
		LogUtil.configureLogging( this, parameters );

		// Print the program header
		printHeader( card );

		log.info( card.getName() + " started " + (isElevated() ? "(elevated)" : "") );
		log.info( "Parameters: " + parameters );

		if( parameters.isSet( UpdateFlag.TITLE ) ) showProgressDialog();

		boolean stdin = parameters.isSet( UpdateFlag.STDIN );
		boolean file = parameters.isSet( UpdateFlag.FILE );
		boolean callback = parameters.isSet( ElevatedHandler.CALLBACK_SECRET );

		if( callback ) {
			runTasksFromSocket();
		} else {
			if( stdin & file ) {
				log.error( "Cannot use both --stream and --file parameters at the same time" );
				return;
			} else if( !(stdin | file) ) {
				log.error( "Must use either --stream or --file to provide update commands" );
				return;
			}

			if( stdin ) runTasksFromStdIn();
			if( file ) runTasksFromFile( new File( parameters.get( UpdateFlag.FILE ) ) );
		}

		log.info( card.getName() + " finished" );

		Runtime.getRuntime().exit( 0 );
	}

	private void showProgressDialog() {
		title = parameters.get( UpdateFlag.TITLE );

		Platform.startup( () -> {} );

		Platform.runLater( () -> {
			alert = new Alert( Alert.AlertType.INFORMATION, "", ButtonType.CANCEL );
			alert.setTitle( title );
			alert.setHeaderText( "Performing update" );

			// The following line is a workaround to dialogs showing with zero size on Linux
			alert.setResizable( true );

			Optional<ButtonType> result = alert.showAndWait();

			if( result.isPresent() && result.get() == ButtonType.CANCEL ) cancelled = true;
		} );
	}

	private List<TaskResult> runTasksFromSocket() throws IOException, InterruptedException {
		String secret = parameters.get( ElevatedHandler.CALLBACK_SECRET );
		int port = Integer.parseInt( parameters.get( ElevatedHandler.CALLBACK_PORT ) );
		if( port < 1 ) return null;

		Socket socket = SocketFactory.getDefault().createSocket( InetAddress.getLoopbackAddress(), port );
		socket.getOutputStream().write( secret.getBytes( TextUtil.CHARSET ) );
		socket.getOutputStream().write( '\n' );
		socket.getOutputStream().flush();
		return runTasksFromStream( socket.getInputStream(), socket.getOutputStream() );
	}

	private List<TaskResult> runTasksFromStdIn() throws IOException, InterruptedException {
		return runTasksFromStream( System.in, System.out );
	}

	private List<TaskResult> runTasksFromFile( File file ) throws IOException, InterruptedException {
		return runTasksFromStream( new FileInputStream( file ), new ByteArrayOutputStream() );
	}

	private List<TaskResult> runTasksFromStream( InputStream input, OutputStream output ) throws IOException, InterruptedException {
		return runTasksFromReader( new InputStreamReader( input, "utf-8" ), new OutputStreamWriter( output, "utf-8" ) );
	}

	public List<TaskResult> runTasksFromString( String commands ) throws IOException, InterruptedException {
		StringReader reader = new StringReader( commands );
		StringWriter writer = new StringWriter();
		return runTasksFromReader( reader, writer );
	}

	List<TaskResult> runTasksFromReader( Reader reader, Writer writer ) throws IOException, InterruptedException {
		return runTasks( reader, writer );
	}

	private List<TaskResult> runTasks( Reader reader, Writer writer ) throws IOException, InterruptedException {
		BufferedReader buffer = new BufferedReader( reader );
		PrintWriter printWriter = new PrintWriter( writer );

		List<TaskResult> results = new ArrayList<>();
		String line;
		while( !cancelled && !TextUtil.isEmpty( line = buffer.readLine() ) ) {
			AnnexTask task = parseTask( line.trim() );

			if( alert != null ) Platform.runLater( () -> alert.setContentText( task.getMessage() ) );

			TaskResult result = executeTask( task );

			results.add( result );

			printWriter.print( result );
			printWriter.print( "\n" );
			printWriter.flush();

			if( result.getStatus() == TaskStatus.FAILURE ) break;
		}
		printWriter.close();

		// Closing the elevated process output stream should cause it to exit
		if( elevatedHandler != null ) elevatedHandler.stop();

		if( alert != null ) Platform.runLater( () -> alert.setContentText( "Update complete" ) );
		Thread.sleep( 500 );

		return results;
	}

	private boolean isElevated() {
		return OperatingSystem.isProcessElevated();
	}

	private void printHeader( ProductCard card ) {
		// These use System.err because System.out is used for communication
		System.err.println( card.getName() + " " + card.getVersion() );
		System.err.println( "Java " + System.getProperty( "java.runtime.version" ) );
	}

	private TaskResult executeTask( AnnexTask task ) {
		TaskResult result;

		log.debug( "Task: " + task.getOriginalLine() );

		try {
			// Validate the task parameters before asking if it needs elevation
			task.validate();

			log.trace( "Task needs elevation?: " + task.needsElevation() );

			if( task.needsElevation() && !isElevated() ) {
				if( elevatedHandler == null ) elevatedHandler = new ElevatedHandler( this ).start();
				result = elevatedHandler.execute( task );
			} else {
				result = task.execute();
			}
		} catch( Exception exception ) {
			log.error( "Error executing task", exception );
			String message = String.format( "%s: %s", exception.getClass().getSimpleName(), exception.getMessage() );
			result = new TaskResult( task, TaskStatus.FAILURE, message );
		}

		log.info( "Result: " + result.getTask().getCommand() + " " + result );

		return result;
	}

	private AnnexTask parseTask( String line ) {
		List<String> commands = TextUtil.split( line );
		String command = commands.get( 0 );
		List<String> parameterList = commands.subList( 1, commands.size() );

		AnnexTask task;
		switch( command ) {
			case UpdateTask.DELETE: {
				task = new DeleteTask( parameterList );
				break;
			}
			case UpdateTask.ECHO: {
				task = new EchoTask( parameterList );
				break;
			}
			case UpdateTask.ELEVATED_ECHO: {
				task = new ElevatedEchoTask( parameterList );
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
