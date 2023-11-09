package com.avereon.weave;

import com.avereon.log.LazyEval;
import com.avereon.util.NonBlockingReader;
import com.avereon.util.OperatingSystem;
import com.avereon.util.ProcessCommands;
import com.avereon.util.TextUtil;
import lombok.CustomLog;

import javax.net.ServerSocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@CustomLog
class ElevatedHandler {

	static final int ELEVATED_PROCESS_WAIT_TIME_SECONDS = 300;

	static final String HEADER = "HEADER";

	static final String MESSAGE = "MESSAGE";

	static final String PROGRESS = "PROGRESS";

	static final String LOG = "LOG";

	private final Weave program;

	private String secret;

	private ServerSocket server;

	private Socket socket;

	private NonBlockingReader reader;

	private Throwable throwable;

	private long serverStart;

	private long clientConnect;

	ElevatedHandler( Weave program ) {
		this.program = program;
	}

	public ElevatedHandler start() throws IOException {
		serverStart = System.currentTimeMillis();
		secret = UUID.randomUUID().toString();
		startServerSocket();
		startProcess();
		return this;
	}

	public ElevatedHandler startAndWait() throws IOException, InterruptedException, TimeoutException {
		start();
		waitForConnect();
		return this;
	}

	public ElevatedHandler stop() throws IOException {
		if( socket != null ) socket.close();
		if( server != null ) server.close();
		return this;
	}

	public synchronized TaskResult execute( Task task ) throws IOException, InterruptedException, TimeoutException {
		waitForConnect();
		return getTaskResult( sendTask( task.setElevated(), false ) );
	}

	public synchronized TaskResult rollback( Task task ) throws IOException, InterruptedException, TimeoutException {
		waitForConnect();
		return getTaskResult( sendTask( task.setElevated(), true ) );
	}

	public long getElevatedHandlerStartDuration() {
		if( clientConnect == 0 ) return 0;
		return clientConnect - serverStart;
	}

	private Task sendTask( Task task, boolean rollback ) throws IOException {
		log.atDebug().log( "Sending task commands to elevated process..." );
		log.atDebug().log( "  commands: %s", task.getOriginalLine() );
		if( rollback ) socket.getOutputStream().write( '-' );
		socket.getOutputStream().write( task.getOriginalLine().getBytes( TextUtil.CHARSET ) );
		socket.getOutputStream().write( '\n' );
		socket.getOutputStream().flush();
		log.atDebug().log( "send > %s", task.getOriginalLine() );
		return task;
	}

	private TaskResult getTaskResult( Task task ) throws IOException {
		log.atDebug().log( "Reading task result from elevated process..." );

		String commandLine;
		while( (commandLine = reader.readLine( 5, TimeUnit.SECONDS )) != null ) {
			log.atTrace().log( "recv < " + commandLine );
			String[] commands = commandLine.split( " " );
			String command = commands[ 0 ];

			final String line = commandLine;

			switch( command ) {
				case HEADER -> task.setHeader( line.substring( HEADER.length() + 1 ) );
				case MESSAGE -> task.setMessage( line.substring( MESSAGE.length() + 1 ) );
				case PROGRESS -> task.incrementProgress();
				case LOG -> log.atInfo().log( "%s", LazyEval.of( () -> line.substring( LOG.length() + 1 ) ) );
				default -> {
					TaskResult result = TaskResult.parse( task, line );
					log.atDebug().log( "  result: %s", result );
					return result;
				}
			}
		}

		return null;
	}

	synchronized void waitForConnect() throws IOException, InterruptedException, TimeoutException {
		if( socket != null ) return;

		// Number of attempts
		int attemptLimit = ELEVATED_PROCESS_WAIT_TIME_SECONDS;

		// Wait for the elevated process to get started
		int attemptCount = 0;
		while( socket == null && attemptCount < attemptLimit && throwable == null ) {
			if( attemptCount > 0 ) log.atTrace().log( "Waiting for elevated process: %s of %s seconds", attemptCount, attemptLimit );
			attemptCount++;
			wait( 1000 );
		}

		if( attemptCount >= attemptLimit ) throw new TimeoutException( "Timeout waiting for elevated updater to start" );
		if( throwable != null ) throw new IOException( throwable );
	}

	private void startServerSocket() throws IOException {
		server = ServerSocketFactory.getDefault().createServerSocket( 0, 1, InetAddress.getLoopbackAddress() );
		new ClientAcceptThread().start();
	}

	private void startProcess() throws IOException {
		// Send the callback port and secret
		ProcessBuilder processBuilder = new ProcessBuilder( ProcessCommands.forLauncher() );
		processBuilder.command().add( ElevatedFlag.CALLBACK_SECRET );
		processBuilder.command().add( secret );
		processBuilder.command().add( ElevatedFlag.CALLBACK_PORT );
		processBuilder.command().add( String.valueOf( server.getLocalPort() ) );

		OperatingSystem.elevateProcessBuilder( program.getTitle(), processBuilder );
		log.atDebug().log( "Elevated commands: %s", LazyEval.of( () -> TextUtil.toString( processBuilder.command(), " " ) ) );

		Process process = processBuilder.inheritIO().start();
		new ProcessWatcherThread( process ).start();
	}

	private synchronized void setSocket( Socket socket ) {
		clientConnect = System.currentTimeMillis();
		this.socket = socket;
		notifyAll();
	}

	private class ClientAcceptThread extends Thread {

		private ClientAcceptThread() {
			setDaemon( true );
		}

		@Override
		public void run() {
			try {
				Socket peer = null;
				while( peer == null ) {
					peer = server.accept();
					reader = new NonBlockingReader( peer.getInputStream() );
					if( reader.readLine( 100, TimeUnit.MILLISECONDS ).equals( secret ) ) {
						log.atDebug().log( "Elevated client connected to normal client: %s", LazyEval.of( () -> server.getLocalPort() ) );
						setSocket( peer );
						server.close();
					}
				}
			} catch( Throwable throwable ) {
				ElevatedHandler.this.throwable = throwable;
			}
		}

	}

	private class ProcessWatcherThread extends Thread {

		private final Process process;

		ProcessWatcherThread( Process process ) {
			this.process = process;
			setDaemon( true );
		}

		public void run() {
			try {
				process.waitFor();
				int exitValue = process.exitValue();
				log.atDebug().log( "Elevated process finished" );
				if( exitValue != 0 ) throw new IllegalStateException( "Elevated process failed: " + exitValue );
			} catch( Exception exception ) {
				throwable = exception;
			}
		}

	}

}
