package com.avereon.weave;

import com.avereon.log.Log;
import com.avereon.product.Product;
import com.avereon.product.ProductCard;
import com.avereon.product.Rb;
import com.avereon.settings.MapSettings;
import com.avereon.settings.Settings;
import com.avereon.util.*;
import com.avereon.weave.task.*;
import com.avereon.zerra.image.Images;
import com.avereon.zerra.javafx.Fx;
import com.avereon.zenna.icon.UpdateIcon;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import lombok.CustomLog;
import lombok.Getter;

import javax.net.SocketFactory;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@CustomLog
public class Weave extends Application implements Product {

	public enum Status {
		STOPPED,
		STARTING,
		STARTED,
		STOPPING
	}

	public static final String DEFAULT_LOG_FILE_PATTERN = "update.%u.log";

	public static final String STYLESHEET_DARK = "weave-dark.css";

	//public static final String STYLESHEET_LIGHT = "weave-light.css";

	private static final Map<String, Class<? extends Task>> taskNameMap;

	@Getter
	private Status status;

	private boolean execute;

	private Thread executeThread;

	private final Object waitLock;

	private final ProductCard card;

	private final Path programDataFolder;

	@Getter
	private final String title;

	private InputSource inputSource;

	private ElevatedHandler elevatedHandler;

	private com.avereon.util.Parameters parameters;

	private Alert alert;

	private ProgressPane progressPane;

	private boolean overallFailure;

	private boolean relaunched;

	static {
		Map<String, Class<? extends Task>> map = new HashMap<>();
		map.put( UpdateTask.DELETE, DeleteTask.class );
		map.put( UpdateTask.ELEVATED_LOG, ElevatedLogTask.class );
		map.put( UpdateTask.ELEVATED_PAUSE, ElevatedPauseTask.class );
		map.put( UpdateTask.EXECUTE, ExecuteTask.class );
		map.put( UpdateTask.FAIL, FailTask.class );
		map.put( UpdateTask.HEADER, HeaderTask.class );
		map.put( UpdateTask.LAUNCH, LaunchTask.class );
		map.put( UpdateTask.LOG, LogTask.class );
		map.put( UpdateTask.MOVE, MoveTask.class );
		map.put( UpdateTask.PAUSE, PauseTask.class );
		map.put( UpdateTask.PERMISSIONS, PermissionsTask.class );
		map.put( UpdateTask.RENAME, MoveTask.class );
		map.put( UpdateTask.UNPACK, UnpackTask.class );

		//noinspection Java9CollectionFactory
		taskNameMap = Collections.unmodifiableMap( map );
	}

	public Weave() {
		Rb.init( this );

		this.execute = true;
		this.status = Status.STOPPED;
		this.waitLock = new Object();
		this.card = ProductCard.info( this );
		this.title = card.getName();
		this.programDataFolder = OperatingSystem.getUserProgramDataFolder( card.getArtifact(), card.getName() );
	}

	@Override
	public ProductCard getCard() {
		return card;
	}

	@Override
	public Settings getSettings() {
		return new MapSettings();
	}

	@Override
	public Path getDataFolder() {
		return programDataFolder;
	}

	/**
	 * This method is used to launch the program from the Xenon launcher.
	 *
	 * @param commands The program commands
	 */
	public static void launch( String... commands ) {
		new Weave().start( commands );
	}

	/**
	 * This method is used to launch the program as a JavaFX application.
	 *
	 * @param stage the primary stage for this application, onto which
	 * the application scene can be set.
	 */
	public void start( Stage stage ) {
		start( getParameters().getRaw().toArray( new String[ 0 ] ) );
	}

	/**
	 * This method is the internal start implementation.
	 *
	 * @param commands The program commands
	 */
	void start( String... commands ) {
		synchronized( this ) {
			status = Status.STARTING;
			this.notifyAll();
		}

		// Parse parameters
		this.parameters = com.avereon.util.Parameters.parse( commands );

		// Configure logging
		configureLogging( parameters );

		// Print the program header
		if( !isElevated() ) printHeader( card );

		if( parameters.isSet( WeaveFlag.HELP ) ) {
			printHelp();
			return;
		}

		log.atInfo().log( "%s %s", card.getName(), card.getRelease() );
		log.atInfo().log( "%s started in %s mode", card.getName(), isElevated() ? "[ELEVATED]" : "[NORMAL]" );
		log.atFine().log( "%sCommand line: %s", elevatedKey(), ProcessCommands.getCommandLineAsString() );
		log.atFine().log( "%sParameters:   %s", elevatedKey(), parameters );
		log.atFine().log( "%sLog: %s", elevatedKey(), Log.getLogFile() );

		boolean file = parameters.isSet( WeaveFlag.FILE );
		boolean stdin = parameters.isSet( WeaveFlag.STDIN );
		boolean string = parameters.isSet( InternalFlag.STRING );
		boolean callback = parameters.isSet( ElevatedFlag.CALLBACK_SECRET );
		boolean update = parameters.isSet( WeaveFlag.UPDATE );

		if( callback ) {
			inputSource = InputSource.SOCKET;
		} else if( string ) {
			inputSource = InputSource.INTERNAL;
		} else {
			int count = 0;
			if( stdin ) count++;
			if( file ) count++;
			if( update ) count++;

			if( count < 1 ) {
				log.atSevere().log( "Missing input source" );
			} else if( count > 1 ) {
				log.atSevere().log( "Cannot only use one input source" );
				return;
			}

			if( stdin ) inputSource = InputSource.STDIN;
			if( file ) inputSource = InputSource.FILE;
			if( update ) inputSource = InputSource.UPDATE;
		}

		if( inputSource != null ) {
			executeThread = new Thread( new Runner() );
			executeThread.setName( "Zenna " + (isElevated() ? "elevated" : "execute") + " thread" );
			executeThread.start();
		}
	}

	private void configureLogging( com.avereon.util.Parameters parameters ) {
		Log.configureLogging( this, parameters, null, DEFAULT_LOG_FILE_PATTERN );
		Log.setPackageLogLevel( "com.avereon", parameters.get( LogFlag.LOG_LEVEL, LogFlag.INFO ) );
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
		if( executeThread != null ) executeThread.interrupt();
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
				log.atFine().log( "Show progress UI=%s", isUi() );
				if( isUi() ) {
					showProgressDialog();
					Thread.sleep( 500 );
				}
				synchronized( Weave.this ) {
					status = Status.STARTED;
					Weave.this.notifyAll();
				}
				execute();
			} catch( Throwable throwable ) {
				log.atSevere().withCause( throwable ).log( "%sExecution error", elevatedKey() );
				throwable.printStackTrace( System.err );
			} finally {
				if( isUi() ) hideProgressDialog();
				synchronized( Weave.this ) {
					status = Status.STOPPED;
					Weave.this.notifyAll();
				}
				log.atInfo().log( "%s%s finished", elevatedKey(), card.getName() );
			}

			if( !TestUtil.isTest() && shouldClose() ) System.exit( 0 );
		}

	}

	private boolean shouldClose() {
		return !overallFailure || relaunched;
	}

	private boolean isUi() {
		return alert != null || (parameters != null && parameters.isSet( WeaveFlag.TITLE ));
	}

	private void execute() throws Exception {
		switch( inputSource ) {
			case FILE: {
				runTasksFromFile( new File( parameters.get( WeaveFlag.FILE ) ) );
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
			case INTERNAL: {
				// runTasksFromString() is called outside this method
				synchronized( waitLock ) {
					while( execute && !Thread.currentThread().isInterrupted() ) {
						waitLock.wait( 1000 );
					}
				}
				break;
			}
			case UPDATE: {
				runTasksFromFile( new File( parameters.get( WeaveFlag.UPDATE ) ) );
				break;
			}
		}
	}

	private void showProgressDialog() {
		// Start JavaFX if necessary
		Fx.startup();

		// Show the progress dialog
		Fx.run( () -> {
			progressPane = new ProgressPane();
			progressPane.setPrefWidth( 400 );
			progressPane.setMessage( "Starting update" );

			alert = new Alert( Alert.AlertType.INFORMATION, "", ButtonType.CANCEL );
			alert.setTitle( parameters.get( WeaveFlag.TITLE ) );
			alert.setHeaderText( "Performing update" );
			alert.getDialogPane().setContent( progressPane );

			Scene scene = alert.getDialogPane().getScene();

			// NOTE Application.setUserAgentStylesheet() must be called in application for this to work properly
			Application.setUserAgentStylesheet( Application.STYLESHEET_MODENA );
			boolean useDarkMode = Boolean.parseBoolean( parameters.get( WeaveFlag.DARK, "false" ) );
			if( useDarkMode ) scene.getStylesheets().addAll( Weave.STYLESHEET_DARK );

			Stage stage = (Stage)scene.getWindow();
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

		try {
			Fx.waitForWithExceptions( 1000 );
		} catch( TimeoutException exception ) {
			log.atWarning().log( "Timeout waiting for progress dialog" );
		} catch( InterruptedException exception ) {
			log.atWarning().log( "Interrupted waiting for progress dialog" );
		}
	}

	private void hideProgressDialog() {
		if( alert != null && shouldClose() ) Fx.run( () -> alert.close() );
	}

	public List<TaskResult> runTasksFromString( String commands ) throws IOException, InterruptedException, TimeoutException {
		StringReader reader = new StringReader( commands );
		StringWriter writer = new StringWriter();
		return runTasksFromReader( reader, writer );
	}

	@SuppressWarnings( "UnusedReturnValue" )
	private List<TaskResult> runTasksFromSocket() throws IOException, InterruptedException, TimeoutException {
		String secret = parameters.get( ElevatedFlag.CALLBACK_SECRET );
		int port = Integer.parseInt( parameters.get( ElevatedFlag.CALLBACK_PORT ) );
		if( port < 1 ) return null;

		try( Socket socket = SocketFactory.getDefault().createSocket( InetAddress.getLoopbackAddress(), port ) ) {
			socket.getOutputStream().write( secret.getBytes( TextUtil.CHARSET ) );
			socket.getOutputStream().write( '\n' );
			socket.getOutputStream().flush();
			return runTasksFromStream( socket.getInputStream(), socket.getOutputStream() );
		}
	}

	@SuppressWarnings( "UnusedReturnValue" )
	private List<TaskResult> runTasksFromStdIn() throws IOException, InterruptedException, TimeoutException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		System.in.transferTo( buffer );
		return runTasksFromStream( new ByteArrayInputStream( buffer.toByteArray() ), System.out );
	}

	@SuppressWarnings( "UnusedReturnValue" )
	private List<TaskResult> runTasksFromFile( File file ) throws IOException, InterruptedException, TimeoutException {
		return runTasksFromStream( new FileInputStream( file ), new ByteArrayOutputStream() );
	}

	private List<TaskResult> runTasksFromStream( InputStream input, OutputStream output ) throws IOException, InterruptedException, TimeoutException {
		return runTasksFromReader( new InputStreamReader( input, TextUtil.CHARSET ), new OutputStreamWriter( output, TextUtil.CHARSET ) );
	}

	List<TaskResult> runTasksFromReader( Reader reader, Writer writer ) throws IOException, InterruptedException, TimeoutException {
		return runTasks( reader, writer );
	}

	private List<TaskResult> runTasks( Reader reader, Writer writer ) throws IOException, InterruptedException, TimeoutException {
		return isElevated() ? runTasksElevated( reader, writer ) : runTasksNormally( reader, writer );
	}

	private List<TaskResult> runTasksElevated( Reader reader, Writer writer ) throws IOException {
		String line;
		List<TaskResult> results = new ArrayList<>();
		PrintWriter printWriter = new PrintWriter( writer );
		NonBlockingReader buffer = new NonBlockingReader( reader );

		// How long to sit around, without receiving any tasks, before giving up
		int hungTimeout = 60;

		// If the elevated process gives up waiting for its next task too soon
		// it exits...closing the socket and causing a broken pipe
		while( !TextUtil.isEmpty( line = buffer.readLine( hungTimeout, TimeUnit.SECONDS ) ) ) {
			line = line.trim();
			boolean rollback = false;
			if( line.startsWith( "-" ) ) {
				rollback = true;
				line = line.substring( 1 );
			}
			Task task = parseTask( line.trim() );
			try {
				TaskHandler handler = new TaskHandler( task.getStepCount(), printWriter );
				task.addTaskListener( handler );
				if( rollback ) {
					results.add( rollbackTask( task, printWriter ) );
				} else {
					results.add( executeTask( task, printWriter ) );
				}
				task.removeTaskListener( handler );
			} catch( Exception exception ) {
				results.add( getTaskResult( task, exception ) );
			}
		}
		return results;
	}

	private List<TaskResult> runTasksNormally( Reader reader, Writer writer ) throws IOException, InterruptedException, TimeoutException {
		String line;
		NonBlockingReader buffer = new NonBlockingReader( reader );
		List<Task> tasks = new ArrayList<>();
		while( !TextUtil.isEmpty( line = buffer.readLine( 1, TimeUnit.SECONDS ) ) ) {
			log.atFiner().log( "%sparsed: %s", elevatedKey(), line.trim() );
			tasks.add( parseTask( line.trim() ) );
		}

		log.atInfo().log( "Task count=%s", tasks.size() );

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

		// Check if any tasks need elevation
		if( anyTaskNeedsElevation( tasks ) ) startElevatedHandler();

		// Execute the tasks
		TaskResult result;
		int taskCompletedCount = 0;
		PrintWriter printer = new PrintWriter( writer );
		TaskHandler handler = new TaskHandler( totalSteps, printer );
		for( Task task : tasks ) {
			if( !execute ) break;

			if( isUi() ) task.addTaskListener( handler );
			results.add( result = executeTask( task, printer ) );
			if( isUi() ) task.removeTaskListener( handler );

			if( result.getStatus() == TaskStatus.FAILURE ) {
				overallFailure = true;
				rollback( results, printer );
				relaunch( tasks, results, printer );
				break;
			}
			taskCompletedCount++;
		}
		printer.close();

		// Closing the elevated process output stream should cause it to exit
		if( elevatedHandler != null ) elevatedHandler.stop();

		if( isUi() ) {
			if( overallFailure ) {
				showFailureState();
			} else {
				showSuccessState();
			}
		}

		synchronized( waitLock ) {
			log.atFine().log( "%sTasks completed: %s", elevatedKey(), taskCompletedCount );
			execute = false;
			waitLock.notifyAll();
		}

		return results;
	}

	private void showSuccessState() throws InterruptedException {
		if( progressPane != null ) {
			progressPane.setMessage( "Update complete" );
			progressPane.setProgress( 1.0 );
		}
		Thread.sleep( 1000 );
	}

	private void showFailureState() throws InterruptedException {
		if( progressPane != null ) {
			progressPane.setMessage( "Update failed" );
			progressPane.setProgress( -1.0 );
		}
		if( alert != null ) {
			Fx.run( () -> {
				alert.getButtonTypes().clear();
				alert.getButtonTypes().addAll( ButtonType.CLOSE );
			} );
		}
		if( relaunched ) Thread.sleep( 1000 );
	}

	private String elevatedKey() {
		return isElevated() ? "+" : "";
	}

	private boolean isElevated() {
		return OperatingSystem.isProcessElevated();
	}

	private void printHeader( ProductCard card ) {
		// These use System.err because System.out is used for communication
		System.err.println( card.getName() + " " + card.getVersion() );
		System.err.println( "Java " + System.getProperty( "java.runtime.version" ) );
	}

	private void printHelp() {
		System.err.println();
		System.err.println( "Usage: weave [options]" );
	}

	private Task parseTask( String line ) {
		List<String> commands = TextUtil.split( line );
		String command = commands.get( 0 );
		List<String> parameterList = commands.subList( 1, commands.size() );

		Task task;
		try {
			Class<? extends Task> taskClass = taskNameMap.get( command );
			if( taskClass == null ) throw new IllegalArgumentException( "Unknown command: " + command );
			task = taskClass.getConstructor( List.class ).newInstance( parameterList );
		} catch( Exception exception ) {
			throw new RuntimeException( exception );
		}

		task.setOriginalLine( line );

		return task;
	}

	private boolean anyTaskNeedsElevation( Collection<Task> tasks ) {
		boolean anyTaskNeedsElevation = false;
		for( Task task : tasks ) {
			boolean taskNeedsElevation = task.needsElevation();
			if( taskNeedsElevation ) log.atFine().log( "Task requested elevation: %s", task );
			anyTaskNeedsElevation = anyTaskNeedsElevation || taskNeedsElevation;
		}
		return anyTaskNeedsElevation;
	}

	private ElevatedHandler startElevatedHandler() throws InterruptedException, TimeoutException, IOException {
		if( elevatedHandler == null && !isElevated() ) elevatedHandler = new ElevatedHandler( this ).startAndWait();
		return elevatedHandler;
	}

	private TaskResult executeTask( Task task, PrintWriter printWriter ) {
		TaskResult result;

		// Try to keep the prompt the same size as the result prompt below
		log.atFine().log( "%sRunning task:   %s", elevatedKey(), task.getOriginalLine() );

		try {
			task.validate();
			task.prerequisites();
			if( task.needsElevation() && !isElevated() ) {
				result = startElevatedHandler().execute( task );
			} else {
				result = task.execute();
			}
		} catch( Exception exception ) {
			result = getTaskResult( task, exception );
		}

		if( result == null ) {
			log.atSevere().log( "Null result executing %s", task );
		} else {
			printWriter.println( result.format() );
			printWriter.flush();
		}

		// Try to keep the prompt the same size as the running prompt above
		log.atInfo().log( "%sResult: %s", elevatedKey(), result );

		return result;
	}

	private TaskResult rollbackTask( Task task, PrintWriter printWriter ) {
		TaskResult result;

		// Try to keep the prompt the same size as the result prompt below
		log.atFine().log( "%sRollback task:  %s", elevatedKey(), task.getOriginalLine() );

		try {
			if( !isElevated() && task.needsElevation() ) {
				result = startElevatedHandler().rollback( task );
			} else {
				result = task.rollback();
			}
		} catch( Exception exception ) {
			result = getTaskResult( task, exception );
			//log.log( Log.WARN, "", exception );
		}

		if( result == null ) {
			log.atSevere().log( "Null result rolling back %s", task );
		} else {
			printWriter.println( result.format() );
			printWriter.flush();
		}

		// Try to keep the prompt the same size as the running prompt above
		log.atInfo().log( "%sResult: %s", elevatedKey(), result );

		return result;
	}

	private TaskResult getTaskResult( Task task, Exception exception ) {
		TaskResult result;
		if( execute ) {
			if( !TestUtil.isTest() ) log.atSevere().withCause( exception ).log( "%sError executing task", elevatedKey() );
			String message = String.format( "%s: %s", exception.getClass().getSimpleName(), exception.getMessage() );
			result = new TaskResult( task, TaskStatus.FAILURE, message );
		} else {
			result = new TaskResult( task, TaskStatus.CANCELLED );
		}
		return result;
	}

	private void rollback( List<TaskResult> results, PrintWriter printer ) {
		List<Task> undoTasks = results.stream().map( TaskResult::getTask ).collect( Collectors.toList() );
		undoTasks = undoTasks.subList( 0, undoTasks.size() - 1 );
		Collections.reverse( undoTasks );
		undoTasks.forEach( t -> results.add( rollbackTask( t, printer ) ) );
	}

	private void relaunch( List<Task> tasks, List<TaskResult> results, PrintWriter printer ) {
		tasks.stream().filter( t -> UpdateTask.LAUNCH.equals( t.getCommand() ) ).findFirst().ifPresent( t -> {
			results.add( executeTask( t, printer ) );
			relaunched = true;
		} );
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
			if( alert != null ) Fx.run( () -> alert.setHeaderText( header ) );
		}

		@Override
		public void updateMessage( String message ) {
			if( isElevated() ) printWriter.println( ElevatedHandler.MESSAGE + " " + message );
			// Don't flush the stream here...not sure why this is a problem but,
			// there is always a following progress event to flush the stream
			//printWriter.flush();
			if( progressPane != null ) Fx.run( () -> progressPane.setMessage( message ) );
		}

		@Override
		public void updateProgress( int step ) {
			this.step++;
			double progress = (double)this.step / (double)totalSteps;
			if( isElevated() ) {
				printWriter.println( ElevatedHandler.PROGRESS );
				printWriter.flush();
			}
			if( progressPane != null ) {
				Fx.run( () -> {
					progressPane.setElevatedStartDelay( Optional.ofNullable( elevatedHandler ).map( ElevatedHandler::getElevatedHandlerStartDuration ).orElse( 0L ) );
					progressPane.setProgress( progress );
				} );
			}
		}

	}

}
