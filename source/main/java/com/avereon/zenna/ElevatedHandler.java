package com.avereon.zenna;

import com.avereon.util.*;

import javax.net.ServerSocketFactory;
import java.io.IOException;
import java.lang.System.Logger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class ElevatedHandler {

	private static final Logger log = Log.get();

	static final String HEADER = "HEADER";

	static final String MESSAGE = "MESSAGE";

	static final String PROGRESS = "PROGRESS";

	static final String LOG = "LOG";

	private final Program program;

	private String secret;

	private ServerSocket server;

	private Socket socket;

	private NonBlockingReader reader;

	private Throwable throwable;

	private long serverStart;

	private long clientConnect;

	ElevatedHandler( Program program ) {
		this.program = program;
	}

	public ElevatedHandler start() throws IOException {
		serverStart = System.currentTimeMillis();
		secret = UUID.randomUUID().toString();
		startServerSocket();
		startElevatedUpdater();
		return this;
	}

	public ElevatedHandler startAndWait() throws IOException, InterruptedException, TimeoutException {
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

		task.setElevated();
		sendTask( task );

		return getTaskResult( task );
	}

	public long getElevatedHandlerStartDuration() {
		if( clientConnect == 0 ) return 0;
		return clientConnect - serverStart;
	}

	private void sendTask( Task task ) throws IOException {
		log.log( Log.DEBUG, "Sending task commands to elevated process..." );
		log.log( Log.DEBUG, "  commands: " + task.getOriginalLine() );
		socket.getOutputStream().write( task.getOriginalLine().getBytes( TextUtil.CHARSET ) );
		socket.getOutputStream().write( '\n' );
		socket.getOutputStream().flush();
		log.log( Log.TRACE, "send > " + task.getOriginalLine() );
	}

	private TaskResult getTaskResult( Task task ) throws IOException {
		log.log( Log.DEBUG, "Reading task result from elevated process..." );

		String line;

		while( (line = reader.readLine( 5, TimeUnit.SECONDS )) != null ) {
			log.log( Log.TRACE, "recv < " + line );
			String[] commands = line.split( " " );
			String command = commands[ 0 ];

			switch( command ) {
				case HEADER: {
					task.setHeader( line.substring( HEADER.length() + 1 ) );
					break;
				}
				case MESSAGE: {
					task.setMessage( line.substring( MESSAGE.length() + 1 ) );
					break;
				}
				case PROGRESS: {
					task.incrementProgress();
					break;
				}
				case LOG: {
					log.log( Log.INFO, line.substring( LOG.length() + 1 ) );
					break;
				}
				default: {
					TaskResult result = TaskResult.parse( task, line );
					log.log( Log.DEBUG, "  result: " + result );
					return result;
				}
			}
		}

		return null;
	}

	synchronized void waitForConnect() throws IOException, InterruptedException, TimeoutException {
		// Number of attempts
		int attemptLimit = 20;
		int attemptDuration = 1000;
		int cyclesPerAttempt = 5;
		int cycleDuration = attemptDuration / cyclesPerAttempt;

		// Wait for the elevated process to get started
		int cycle = 0;
		int attemptCount = 0;
		while( socket == null && attemptCount < attemptLimit && throwable == null ) {
			if( cycle++ % cyclesPerAttempt == 0 ) {
				if( attemptCount > 0 ) log.log( Log.TRACE, "Waiting for elevated process: " + attemptCount + " of " + attemptLimit + " seconds" );
				attemptCount++;
			}
			wait( cycleDuration );
		}

		if( attemptCount >= attemptLimit ) throw new TimeoutException( "Timeout waiting for elevated updater to start" );
		if( throwable != null ) throw new IOException( throwable );
	}

	private void startServerSocket() throws IOException {
		server = ServerSocketFactory.getDefault().createServerSocket( 0, 1, InetAddress.getLoopbackAddress() );
		new ClientAcceptThread().start();
	}

	private void startElevatedUpdater() throws IOException {
		// Send the callback port and secret
		ProcessBuilder processBuilder = new ProcessBuilder( ProcessCommands.forLauncher() );
		processBuilder.command().add( ElevatedFlag.CALLBACK_SECRET );
		processBuilder.command().add( secret );
		processBuilder.command().add( ElevatedFlag.CALLBACK_PORT );
		processBuilder.command().add( String.valueOf( server.getLocalPort() ) );

		OperatingSystem.elevateProcessBuilder( program.getTitle(), processBuilder );
		log.log( Log.DEBUG, "Elevated commands: " + TextUtil.toString( processBuilder.command(), " " ) );

		Process process = processBuilder.inheritIO().start();
		new ProcessWatcherThread( process ).start();
	}

	private synchronized void setSocket( Socket socket ) {
		this.socket = socket;
		clientConnect = System.currentTimeMillis();
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
						log.log( Log.DEBUG, "Elevated client connected to normal client: " + server.getLocalPort() );
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
				log.log( Log.DEBUG, "Elevated process finished" );
				if( exitValue != 0 ) throw new IllegalStateException( "Elevated process failed: " + exitValue );
			} catch( Exception exception ) {
				throwable = exception;
			}
		}

	}

}
