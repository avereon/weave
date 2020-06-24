package com.avereon.zenna;

import com.avereon.product.Product;
import com.avereon.product.ProductBundle;
import com.avereon.product.ProductCard;
import com.avereon.rossa.icon.UpdateIcon;
import com.avereon.util.*;
import com.avereon.venza.image.Images;
import com.avereon.zenna.task.*;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import javax.net.SocketFactory;
import java.io.*;
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

	private static final System.Logger log = Log.get();

	private Status status;

	private boolean execute;

	private Thread executeThread;

	private final Object waitLock;

	private final ProductCard card;

	private final ProductBundle resourceBundle;

	private final Path programDataFolder;

	private final String title;

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
		this.resourceBundle = new ProductBundle( this );
		this.programDataFolder = OperatingSystem.getUserProgramDataFolder( card.getArtifact(), card.getName() );
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
	public ProductBundle rb() {
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

	public void configAndStart( String... commands ) {
		// Parse parameters
		parameters = Parameters.parse( commands );

		// Configure logging
		Log.configureLogging( this, parameters );
		Log.setPackageLogLevel( "com.avereon", parameters.get( LogFlag.LOG_LEVEL, LogFlag.INFO ) );

		// Print the program header
		if( !isElevated() ) printHeader( card );

		start( commands );
	}

	public void start( String... commands ) {
		if( parameters == null ) parameters = Parameters.parse( commands );

		synchronized( this ) {
			status = Status.STARTING;
			this.notifyAll();
		}

		log.log( Log.INFO, elevatedKey() + card.getName() + " started " + (isElevated() ? "[ELEVATED]" : "[NORMAL]") );
		log.log( Log.DEBUG, elevatedKey() + "Command line: " + ProcessCommands.getCommandLineAsString() );
		log.log( Log.DEBUG, elevatedKey() + "Parameters:   " + parameters );
		log.log( Log.DEBUG, elevatedKey() + "Log: " + Log.getLogFile() );

		boolean file = parameters.isSet( UpdateFlag.FILE );
		boolean stdin = parameters.isSet( UpdateFlag.STDIN );
		boolean string = parameters.isSet( InternalFlag.STRING );
		boolean callback = parameters.isSet( ElevatedFlag.CALLBACK_SECRET );

		if( callback ) {
			inputSource = InputSource.SOCKET;
		} else if( string ) {
			inputSource = InputSource.STRING;
		} else {
			if( stdin & file ) {
				log.log( Log.ERROR, "Cannot use both --" + InputSource.STDIN + " and --" + InputSource.FILE + " parameters at the same time" );
				return;
			} else if( !(stdin | file) ) {
				log.log( Log.ERROR, "Must use either --" + InputSource.STDIN + " or --" + InputSource.FILE + " to provide update commands" );
				return;
			}
			if( stdin ) inputSource = InputSource.STDIN;
			if( file ) inputSource = InputSource.FILE;
		}

		executeThread = new Thread( new Runner() );
		executeThread.setName( "Zenna " + (isElevated() ? "elevated" : "execute") + " thread" );
		executeThread.start();
	}

	@SuppressWarnings( "SameParameterValue" )
	synchronized void waitForStart( long time, TimeUnit unit ) throws InterruptedException, TimeoutException {
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
				log.log( Log.DEBUG, "Show progress UI=" + isUi() );
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
				log.log( Log.ERROR, elevatedKey() + "Execution error", throwable );
				throwable.printStackTrace( System.err );
			} finally {
				if( isUi() ) hideProgressDialog();
				synchronized( Program.this ) {
					status = Status.STOPPED;
					Program.this.notifyAll();
				}
				log.log( Log.INFO, elevatedKey() + card.getName() + " finished" );
			}
		}

	}

	private boolean isUi() {
		return alert != null || (parameters != null && parameters.isSet( UpdateFlag.TITLE ));
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
		Platform.startup( () -> {
			progressPane = new ProgressPane();
			progressPane.setPrefWidth( 400 );
			progressPane.setMessage( "Starting update" );

			alert = new Alert( Alert.AlertType.INFORMATION, "", ButtonType.CANCEL );
			alert.setTitle( parameters.get( UpdateFlag.TITLE ) );
			alert.setHeaderText( "Performing update" );
			alert.getDialogPane().setContent( progressPane );

			Stage stage = (Stage)alert.getDialogPane().getScene().getWindow();
			stage.getIcons().addAll( Images.getStageIcons( new UpdateIcon() ) );

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
		String secret = parameters.get( ElevatedFlag.CALLBACK_SECRET );
		int port = Integer.parseInt( parameters.get( ElevatedFlag.CALLBACK_PORT ) );
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
			log.log( Log.TRACE, elevatedKey() + "parsed: " + line.trim() );
			tasks.add( parseTask( line.trim() ) );
		}

		log.log( Log.INFO, "Task count=" + tasks.size() );

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
			if( progressPane != null ) progressPane.setMessage( "Update complete" );
			if( progressPane != null ) progressPane.setProgress( 1.0 );
			Thread.sleep( 1000 );
		}

		synchronized( waitLock ) {
			log.log( Log.DEBUG, elevatedKey() + "Tasks completed: " + taskCompletedCount );
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

		// Try to keep the prompt the same size as the result prompt below
		log.log( Log.DEBUG, elevatedKey() + "Running task:   " + task.getOriginalLine() );

		try {
			task.validate();

			boolean needsElevation = task.needsElevation();
			log.log( Log.TRACE, elevatedKey() + "Task needs elevation?: " + needsElevation );
			if( needsElevation && !isElevated() ) {
				if( elevatedHandler == null ) elevatedHandler = new ElevatedHandler( this ).start();
				result = elevatedHandler.execute( task );
			} else {
				result = task.execute();
			}
		} catch( Exception exception ) {
			result = getTaskResult( task, exception );
			log.log( Log.WARN, "", exception );
		}

		if( result == null ) {
			log.log( Log.ERROR, "Null result executing " + task );
		} else {
			printWriter.println( result.format() );
			printWriter.flush();
		}

		// Try to keep the prompt the same size as the running prompt above
		log.log( Log.INFO, elevatedKey() + "Result: " + result );

		return result;
	}

	private TaskResult getTaskResult( Task task, Exception exception ) {
		TaskResult result;
		if( execute ) {
			if( !TestUtil.isTest() ) log.log( Log.ERROR, elevatedKey() + "Error executing task", exception );
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
			case UpdateTask.LOG: {
				task = new LogTask( parameterList );
				break;
			}
			case UpdateTask.ELEVATED_LOG: {
				task = new ElevatedLogTask( parameterList );
				break;
			}
			case UpdateTask.EXECUTE: {
				task = new ExecuteTask( parameterList );
				break;
			}
			case UpdateTask.HEADER: {
				task = new HeaderTask( parameterList );
				break;
			}
			case UpdateTask.LAUNCH: {
				task = new LaunchTask( parameterList );
				break;
			}
			case UpdateTask.MOVE:
			case UpdateTask.RENAME: {
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

		private final int totalSteps;

		private final PrintWriter printWriter;

		TaskHandler( int totalSteps, PrintWriter printWriter ) {
			this.totalSteps = totalSteps;
			this.printWriter = printWriter;
		}

		@Override
		public void updateHeader( String header ) {
			if( isElevated() ) printWriter.println( ElevatedHandler.HEADER + " " + header );
			// Don't flush the stream here...not sure why this is a problem but,
			// there is always a following progress event to flush the stream
			//printWriter.flush();
			if( alert != null ) Platform.runLater( () -> alert.setHeaderText( header ) );
		}

		@Override
		public void updateMessage( String message ) {
			if( isElevated() ) printWriter.println( ElevatedHandler.MESSAGE + " " + message );
			// Don't flush the stream here...not sure why this is a problem but,
			// there is always a following progress event to flush the stream
			//printWriter.flush();
			if( progressPane != null ) Platform.runLater( () -> progressPane.setMessage( message ) );
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
