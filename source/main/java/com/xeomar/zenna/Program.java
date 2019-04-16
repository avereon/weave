package com.xeomar.zenna;

import com.xeomar.product.Product;
import com.xeomar.product.ProductBundle;
import com.xeomar.product.ProductCard;
import com.xeomar.util.*;
import com.xeomar.zenna.task.*;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Program implements Product {

	public enum Status {
		STOPPED,
		STARTING,
		STARTED,
		STOPPING
	}

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private Status status;

	private boolean execute;

	private Thread executeThread;

	private final Object waitLock;

	private ProductCard card;

	private ProductBundle resourceBundle;

	private Path programDataFolder;

	private String title;

	private InputSource inputSource;

	private ElevatedHandler elevatedHandler;

	private Parameters parameters;

	private Alert alert;

	private ProgressPane progressPane;

	public Program() {
		this.execute = true;
		this.status = Status.STOPPED;
		this.waitLock = new Object();
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
		new Program().start( commands );
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

	public Status getStatus() {
		return this.status;
	}

	public void start( String... commands ) {
		synchronized( this ) {
			status = Status.STARTING;
			this.notifyAll();
		}

		// Parse parameters
		parameters = Parameters.parse( commands );

		// Configure logging
		LogUtil.configureLogging( this, parameters );

		// Print the program header
		if( !isElevated() ) printHeader( card );

		log.info( elevatedKey() + card.getName() + " started " + (isElevated() ? "[ELEVATED]" : "") );
		log.info( elevatedKey() + "Parameters: " + parameters );
		log.debug( elevatedKey() + "Command line: " + ProcessCommands.getCommandLineAsString() );
		log.debug( elevatedKey() + "Log: " + LogUtil.getLogFile() );

		boolean file = parameters.isSet( UpdateFlag.FILE );
		boolean stdin = parameters.isSet( UpdateFlag.STDIN );
		boolean string = parameters.isSet( InternalFlag.STRING );
		boolean callback = parameters.isSet( ElevatedHandler.CALLBACK_SECRET );

		if( callback ) {
			inputSource = InputSource.SOCKET;
		} else if( string ) {
			inputSource = InputSource.STRING;
		} else {
			if( stdin & file ) {
				log.error( "Cannot use both --stream and --file parameters at the same time" );
				return;
			} else if( !(stdin | file) ) {
				log.error( "Must use either --stream or --file to provide update commands" );
				return;
			}
			if( stdin ) inputSource = InputSource.STDIN;
			if( file ) inputSource = InputSource.FILE;
		}

		executeThread = new Thread( new Runner() );
		executeThread.setName( "Annex execute thread" );
		executeThread.start();
	}

	@SuppressWarnings( "SameParameterValue" )
	synchronized void waitForStart( long time, TimeUnit unit) throws InterruptedException, TimeoutException {
		while( status != Status.STARTED ) {
			wait( unit.toMillis( time ) );
			if( status != Status.STARTED ) throw new TimeoutException( "Timeout waiting for program to start" );
		}
	}

	public synchronized void stop() {
		if( status != Status.STOPPED ) status = Status.STOPPING;
		execute = false;
		this.notifyAll();
		executeThread.interrupt();
	}

	@SuppressWarnings( "SameParameterValue" )
	synchronized void waitForStop( long time, TimeUnit unit ) throws InterruptedException, TimeoutException {
		while( status != Status.STOPPED ) {
			wait( unit.toMillis( time ) );
			if( status != Status.STOPPED ) throw new TimeoutException( "Timeout waiting for program to stop" );
		}
	}

	private class Runner implements Runnable {

		@Override
		public void run() {
			try {
				if( isUi() ) {
					showProgressDialog();
					Thread.sleep( 500 );
				}
				synchronized( Program.this ) {
					status = Status.STARTED;
					Program.this.notifyAll();
				}
				execute();
			} catch( Throwable throwable ) {
				throwable.printStackTrace( System.err );
				log.error( elevatedKey() + "Execution error", throwable );
			} finally {
				if( isUi() ) hideProgressDialog();
				synchronized( Program.this ) {
					status = Status.STOPPED;
					Program.this.notifyAll();
				}
				log.info( elevatedKey() + card.getName() + " finished" );
			}
		}

	}

	private boolean isUi() {
		return parameters != null && parameters.isSet( UpdateFlag.TITLE ) || alert != null;
	}

	private void execute() throws Exception {
		switch( inputSource ) {
			case FILE: {
				runTasksFromFile( new File( parameters.get( UpdateFlag.FILE ) ) );
				break;
			}
			case STDIN: {
				runTasksFromStdIn();
				break;
			}
			case SOCKET: {
				runTasksFromSocket();
				break;
			}
			case STRING: {
				// runTasksFromString() is called outside this method
				synchronized( waitLock ) {
					while( execute && !Thread.currentThread().isInterrupted() ) {
						waitLock.wait( 1000 );
					}
				}
				break;
			}
		}

	}

	private void showProgressDialog() {
		Platform.startup( () -> {} );

		Platform.runLater( () -> {
			progressPane = new ProgressPane();
			progressPane.setPrefWidth( 400 );
			progressPane.setText( "Starting update" );

			alert = new Alert( Alert.AlertType.INFORMATION, "", ButtonType.CANCEL );
			alert.setTitle( parameters.get( UpdateFlag.TITLE ) );
			alert.setHeaderText( "Performing update" );
			alert.getDialogPane().setContent( progressPane );

			// The following line is a workaround to dialogs showing with zero size on Linux
			alert.setResizable( true );

			// Set the onHidden handler
			alert.setOnHidden( ( event ) -> {
				Optional<ButtonType> result = Optional.ofNullable( alert.getResult() );
				if( result.isPresent() && result.get() == ButtonType.CANCEL ) stop();
			} );

			alert.show();
		} );
	}

	private void hideProgressDialog() {
		if( alert != null ) Platform.runLater( () -> alert.close() );
	}

	public List<TaskResult> runTasksFromString( String commands ) throws IOException, InterruptedException {
		StringReader reader = new StringReader( commands );
		StringWriter writer = new StringWriter();
		return runTasksFromReader( reader, writer );
	}

	@SuppressWarnings( "UnusedReturnValue" )
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

	@SuppressWarnings( "UnusedReturnValue" )
	private List<TaskResult> runTasksFromStdIn() throws IOException, InterruptedException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		System.in.transferTo( buffer );
		return runTasksFromStream( new ByteArrayInputStream( buffer.toByteArray() ), System.out );
	}

	@SuppressWarnings( "UnusedReturnValue" )
	private List<TaskResult> runTasksFromFile( File file ) throws IOException, InterruptedException {
		return runTasksFromStream( new FileInputStream( file ), new ByteArrayOutputStream() );
	}

	private List<TaskResult> runTasksFromStream( InputStream input, OutputStream output ) throws IOException, InterruptedException {
		return runTasksFromReader( new InputStreamReader( input, TextUtil.CHARSET ), new OutputStreamWriter( output, TextUtil.CHARSET ) );
	}

	List<TaskResult> runTasksFromReader( Reader reader, Writer writer ) throws IOException, InterruptedException {
		return runTasks( reader, writer );
	}

	private List<TaskResult> runTasks( Reader reader, Writer writer ) throws IOException, InterruptedException {
		return isElevated() ? runTasksElevated( reader, writer ) : runTasksNormally( reader, writer );
	}

	private List<TaskResult> runTasksElevated( Reader reader, Writer writer ) throws IOException {
		String line;
		List<TaskResult> results = new ArrayList<>();
		NonBlockingReader buffer = new NonBlockingReader( reader );
		while( !TextUtil.isEmpty( line = buffer.readLine( 1, TimeUnit.SECONDS ) ) ) {
			Task task = parseTask( line.trim() );
			try {
				int totalSteps = task.getStepCount();
				PrintWriter printWriter = new PrintWriter( writer );
				TaskHandler handler = new TaskHandler( totalSteps, printWriter );
				task.addTaskListener( handler );
				results.add( executeTask( task, printWriter ) );
				task.removeTaskListener( handler );
			} catch( Exception exception ) {
				results.add( getTaskResult( task, exception ) );
			}
		}
		return results;
	}

	private List<TaskResult> runTasksNormally( Reader reader, Writer writer ) throws IOException, InterruptedException {
		String line;
		NonBlockingReader buffer = new NonBlockingReader( reader );
		List<Task> tasks = new ArrayList<>();
		while( !TextUtil.isEmpty( line = buffer.readLine( 1, TimeUnit.SECONDS ) ) ) {
			log.trace( elevatedKey() + "parsed: " + line.trim() );
			tasks.add( parseTask( line.trim() ) );
		}

		List<TaskResult> results = new ArrayList<>();

		// Validate the tasks and determine the step count
		int totalSteps = 0;
		for( Task task : tasks ) {
			try {
				totalSteps += task.getStepCount();
			} catch( Exception exception ) {
				results.add( getTaskResult( task, exception ) );
			}
		}
		if( results.size() > 0 ) return results;

		// Execute the tasks
		TaskResult result;
		int taskCompletedCount = 0;
		PrintWriter printWriter = new PrintWriter( writer );
		TaskHandler handler = new TaskHandler( totalSteps, printWriter );
		for( Task task : tasks ) {
			if( !execute ) break;

			if( isUi() ) task.addTaskListener( handler );
			results.add( result = executeTask( task, printWriter ) );
			if( isUi() ) task.removeTaskListener( handler );

			if( result.getStatus() == TaskStatus.FAILURE ) break;
			taskCompletedCount++;
		}
		printWriter.close();

		// Closing the elevated process output stream should cause it to exit
		if( elevatedHandler != null ) elevatedHandler.stop();

		if( isUi() ) {
			if( progressPane != null ) Platform.runLater( () -> progressPane.setText( "Update complete" ) );
			if( progressPane != null ) Platform.runLater( () -> progressPane.setProgress( 1.0 ) );
			Thread.sleep( 1000 );
		}

		synchronized( waitLock ) {
			log.debug( elevatedKey() + "Tasks completed: " + taskCompletedCount );
			execute = false;
			waitLock.notifyAll();
		}

		return results;
	}

	private String elevatedKey() {
		return isElevated() ? "*" : "";
	}

	private TaskResult executeTask( Task task, PrintWriter printWriter ) {
		TaskResult result;

		log.debug( elevatedKey() + "Task: " + task.getOriginalLine() );

		try {
			task.validate();

			log.trace( elevatedKey() + "Task needs elevation?: " + task.needsElevation() );
			if( !isElevated() && task.needsElevation() ) {
				if( elevatedHandler == null ) elevatedHandler = new ElevatedHandler( this ).start();
				result = elevatedHandler.execute( task );
			} else {
				result = task.execute();
			}
		} catch( Exception exception ) {
			result = getTaskResult( task, exception );
			log.warn( "", exception );
		}

		printWriter.println( result.format() );
		printWriter.flush();

		log.info( elevatedKey() + "Result: " + result );

		return result;
	}

	private TaskResult getTaskResult( Task task, Exception exception ) {
		TaskResult result;
		if( execute ) {
			if( !TestUtil.isTest() ) log.error( elevatedKey() + "Error executing task", exception );
			String message = String.format( "%s: %s", exception.getClass().getSimpleName(), exception.getMessage() );
			result = new TaskResult( task, TaskStatus.FAILURE, message );
		} else {
			result = new TaskResult( task, TaskStatus.CANCELLED );
		}
		return result;
	}

	private Task parseTask( String line ) {
		List<String> commands = TextUtil.split( line );
		String command = commands.get( 0 );
		List<String> parameterList = commands.subList( 1, commands.size() );

		Task task;
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
			case UpdateTask.EXECUTE: {
				task = new ExecuteTask( parameterList );
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
			case UpdateTask.PERMISSIONS: {
				task = new PermissionsTask( parameterList );
				break;
			}
			case UpdateTask.PAUSE: {
				task = new PauseTask( parameterList );
				break;
			}
			case UpdateTask.RENAME: {
				task = new MoveTask( parameterList );
				break;
			}
			case UpdateTask.ELEVATED_PAUSE: {
				task = new ElevatedPauseTask( parameterList );
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

	private void printHeader( ProductCard card ) {
		// These use System.err because System.out is used for communication
		System.err.println( card.getName() + " " + card.getVersion() );
		System.err.println( "Java " + System.getProperty( "java.runtime.version" ) );
	}

	private boolean isElevated() {
		return OperatingSystem.isProcessElevated();
	}

	private class TaskHandler implements TaskListener {

		private int step;

		private int totalSteps;

		private PrintWriter printWriter;

		TaskHandler( int totalSteps, PrintWriter printWriter ) {
			this.totalSteps = totalSteps;
			this.printWriter = printWriter;
		}

		@Override
		public void updateMessage( String message ) {
			if( isElevated() ) {
				printWriter.println( ElevatedHandler.MESSAGE + " " + message );
				// Don't flush the stream here...not sure why this is a problem but,
				// there is always a following progress event to flush the stream
				//printWriter.flush();
			}
			if( progressPane != null ) Platform.runLater( () -> progressPane.setText( message ) );
		}

		@Override
		public void updateProgress( int step ) {
			this.step++;
			double progress = (double)this.step / (double)totalSteps;
			if( isElevated() ) {
				printWriter.println( ElevatedHandler.PROGRESS );
				printWriter.flush();
			}
			if( progressPane != null ) Platform.runLater( () -> progressPane.setProgress( progress ) );
		}

	}

}
