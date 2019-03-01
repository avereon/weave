package com.xeomar.xevra;

import com.xeomar.util.*;
import org.slf4j.Logger;

import javax.net.ServerSocketFactory;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class ElevatedHandler {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	static final String CALLBACK_SECRET = "--callback-secret";

	static final String CALLBACK_PORT = "--callback-port";

	static final String MESSAGE = "MESSAGE";

	static final String PROGRESS = "PROGRESS";

	static final String LOG = "LOG";

	private Program program;

	private String secret;

	private ServerSocket server;

	private Socket socket;

	private Throwable throwable;

	ElevatedHandler( Program program ) {
		this.program = program;
	}

	public ElevatedHandler start() throws IOException {
		startServerSocket();
		startElevatedUpdater();
		return this;
	}

	public ElevatedHandler stop() throws IOException {
		if( socket != null ) socket.close();
		if( server != null ) server.close();
		return this;
	}

	public synchronized TaskResult execute( AbstractUpdateTask task ) throws IOException, InterruptedException, TimeoutException {
		waitForSocket();

		task.setElevated();
		sendTask( task );

		return getTaskResult( task );
	}

	private void sendTask( AbstractUpdateTask task ) throws IOException {
		log.debug( "Sending task commands to elevated process..." );
		log.debug( "  commands: " + task.getOriginalLine() );
		socket.getOutputStream().write( task.getOriginalLine().getBytes( TextUtil.CHARSET ) );
		socket.getOutputStream().write( '\n' );
		socket.getOutputStream().flush();
		log.warn( "send > " + task.getOriginalLine() );
	}

	private TaskResult getTaskResult( AbstractUpdateTask task ) throws IOException, InterruptedException {
		log.debug( "Reading task result from elevated process..." );

		NonBlockingReader reader = new NonBlockingReader( socket.getInputStream() );
		String line;

		while( (line = reader.readLine( 5, TimeUnit.SECONDS )) != null ) {
			log.warn( "recv < " + line );
			String[] commands = line.split( " " );
			String command = commands[ 0 ];

			switch( command ) {
				case MESSAGE: {
					task.setMessage( line.substring( MESSAGE.length() + 1 ) );
					break;
				}
				case PROGRESS: {
					task.incrementProgress();
					break;
				}
				case LOG: {
					log.info( line.substring( LOG.length() + 1 ) );
					break;
				}
				default: {
					TaskResult result = TaskResult.parse( task, line );
					log.debug( "  result: " + result );
					return result;
				}
			}
		}

		return null;
	}

	private void waitForSocket() throws IOException, InterruptedException, TimeoutException {
		// Wait for the elevated process to get started
		int index = 0;
		int attemptCount = 0;
		int attemptLimit = 20;
		while( socket == null && attemptCount < attemptLimit && throwable == null ) {
			if( index++ % 10 == 0 ) {
				if( attemptCount > 0 ) log.trace( "Waiting for elevated process: " + attemptCount + " of " + attemptLimit + " seconds" );
				attemptCount++;
			}
			wait( 100 );
		}

		if( attemptCount >= attemptLimit ) throw new TimeoutException( "Timeout waiting for elevated updater to start" );
		if( throwable != null ) throw new IOException( throwable );
	}

	private void startServerSocket() throws IOException {
		server = ServerSocketFactory.getDefault().createServerSocket( 0, 1, InetAddress.getLoopbackAddress() );
		new ClientAcceptThread().start();
	}

	private void startElevatedUpdater() throws IOException {
		secret = UUID.randomUUID().toString();

		ProcessBuilder processBuilder = new ProcessBuilder( ProcessCommands.forModule() );
		processBuilder.inheritIO();

		// Send the callback port and secret
		processBuilder.command().add( CALLBACK_SECRET );
		processBuilder.command().add( secret );
		processBuilder.command().add( CALLBACK_PORT );
		processBuilder.command().add( String.valueOf( server.getLocalPort() ) );
		processBuilder.command().add( LogFlag.LOG_LEVEL );
		processBuilder.command().add( LogFlag.NONE );

		OperatingSystem.elevateProcessBuilder( program.getTitle(), processBuilder );
		log.debug( "Elevated commands: " + TextUtil.toString( processBuilder.command(), " " ) );
		processBuilder.inheritIO();
		Process process = processBuilder.start();
		new ProcessWatcherThread( process ).start();
	}

	private synchronized void setSocket( Socket socket ) {
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
					NonBlockingReader reader = new NonBlockingReader( peer.getInputStream() );
					if( reader.readLine( 100, TimeUnit.MILLISECONDS ).equals( secret ) ) {
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

		private Process process;

		private int exitValue;

		ProcessWatcherThread( Process process ) {
			this.process = process;
			setDaemon( true );
		}

		public void run() {
			try {
				process.waitFor();
				exitValue = process.exitValue();
				if( exitValue != 0 ) throw new IllegalStateException( "Elevated process failed: " + exitValue );
			} catch( Exception exception ) {
				throwable = exception;
			}
		}

		public int getExitValue() {
			return exitValue;
		}

	}

}
