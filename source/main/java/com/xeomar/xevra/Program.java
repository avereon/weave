package com.xeomar.xevra;

import com.xeomar.product.Product;
import com.xeomar.product.ProductBundle;
import com.xeomar.product.ProductCard;
import com.xeomar.util.*;
import com.xeomar.xevra.task.*;
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

	private boolean execute;

	private boolean started;

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
		this.started = false;
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

	public void start( String[] commands ) {
		// Parse parameters
		parameters = Parameters.parse( commands );

		// Configure logging
		LogUtil.configureLogging( this, parameters );

		// Print the program header
		printHeader( card );

		log.info( card.getName() + " started " + (isElevated() ? "(elevated)" : "") );
		log.info( "Parameters: " + parameters );

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

	public synchronized void waitForStart() throws InterruptedException {
		while( !started ) {
			wait( 1000 );
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
				synchronized( this ) {
					started = true;
					this.notifyAll();
				}
				execute();
			} catch( Throwable throwable ) {
				throwable.printStackTrace( System.err );
				log.error( "Execution error", throwable );
			} finally {
				if( isUi() ) hideProgressDialog();
				log.info( card.getName() + " finished" );
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

	private void terminate() {
		execute = false;
		executeThread.interrupt();
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
				if( result.isPresent() && result.get() == ButtonType.CANCEL ) terminate();
			} );

			alert.show();
		} );
	}

	private void hideProgressDialog() {
		Platform.runLater( () -> {
			if( alert != null ) alert.close();
		} );
	}

	public List<TaskResult> runTasksFromString( String commands ) throws IOException, InterruptedException {
		StringReader reader = new StringReader( commands );
		StringWriter writer = new StringWriter();
		return runTasksFromReader( reader, writer );
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
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		System.in.transferTo( buffer );
		return runTasksFromStream( new ByteArrayInputStream( buffer.toByteArray() ), System.out );
	}

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
		BufferedReader buffer = new BufferedReader( reader );
		PrintWriter printWriter = new PrintWriter( writer );

		String line;
		List<AbstractUpdateTask> tasks = new ArrayList<>();
		while( !TextUtil.isEmpty( line = buffer.readLine() ) ) {
			AbstractUpdateTask task = parseTask( line.trim() );
			tasks.add( task );
			//log.debug( line.trim() );
		}

		// List the commands that were read in
		for( AbstractUpdateTask task : tasks ) {
			log.warn( "Parsed line: " + task.getOriginalLine() );
		}

		List<TaskResult> results = new ArrayList<>();

		// Validate the tasks and determine the step count
		int totalSteps = 0;
		for( AbstractUpdateTask task : tasks ) {
			try {
				task.validate();
				totalSteps += task.getStepCount();
			} catch( Exception exception ) {
				results.add( getTaskResult( task, exception ) );
			}
		}
		if( results.size() > 0 ) return results;


		// Execute the tasks
		TaskResult result;
		int taskCompletedCount = 0;
		TaskHandler handler = new TaskHandler( totalSteps );
		for( AbstractUpdateTask task : tasks ) {
			if( !execute ) break;

			if( isUi() ) task.addTaskListener( handler );
			results.add( result = executeTask( task ) );
			if( isUi() ) task.removeTaskListener( handler );

			printWriter.print( result );
			printWriter.print( "\n" );
			printWriter.flush();

			if( result.getStatus() == TaskStatus.FAILURE ) break;
			taskCompletedCount++;
		}
		printWriter.close();

		// Closing the elevated process output stream should cause it to exit
		if( elevatedHandler != null ) elevatedHandler.stop();

		if( isUi() ) {
			Platform.runLater( () -> {
				if( progressPane != null ) progressPane.setText( "Update complete" );
			} );
			Thread.sleep( 1000 );
		}

		synchronized( waitLock ) {
			log.debug( "Tasks completed: " + taskCompletedCount );
			execute = false;
			waitLock.notifyAll();
		}

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

	private TaskResult executeTask( AbstractUpdateTask task ) {
		TaskResult result;

		log.debug( "Task: " + task.getOriginalLine() );

		try {
			log.trace( "Task needs elevation?: " + task.needsElevation() );
			if( task.needsElevation() && !isElevated() ) {
				if( elevatedHandler == null ) elevatedHandler = new ElevatedHandler( this ).start();
				result = elevatedHandler.execute( task );
			} else {
				result = task.execute();
			}
		} catch( Exception exception ) {
			result = getTaskResult( task, exception );
		}

		log.info( "Result: " + result.getTask().getCommand() + " " + result );

		return result;
	}

	private TaskResult getTaskResult( AbstractUpdateTask task, Exception exception ) {
		TaskResult result;
		if( execute ) {
			if( !TestUtil.isTest() ) log.error( "Error executing task", exception );
			String message = String.format( "%s: %s", exception.getClass().getSimpleName(), exception.getMessage() );
			result = new TaskResult( task, TaskStatus.FAILURE, message );
		} else {
			result = new TaskResult( task, TaskStatus.CANCELLED );
		}
		return result;
	}

	private AbstractUpdateTask parseTask( String line ) {
		List<String> commands = TextUtil.split( line );
		String command = commands.get( 0 );
		List<String> parameterList = commands.subList( 1, commands.size() );

		AbstractUpdateTask task;
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

	private class TaskHandler implements TaskListener {

		private int step;

		private int totalSteps;

		public TaskHandler( int totalSteps ) {
			this.totalSteps = totalSteps;
		}

		@Override
		public void updateMessage( String message ) {
			Platform.runLater( () -> {
				if( progressPane != null ) progressPane.setText( message );
			} );
		}

		@Override
		public void updateProgress( int step ) {
			this.step++;
			double progress = (double)this.step / (double)totalSteps;
			Platform.runLater( () -> {
				if( progressPane != null ) progressPane.setProgress( progress );
			} );
		}

	}

}
