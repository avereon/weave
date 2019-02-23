package com.xeomar.xevra;

import com.xeomar.util.*;
import org.slf4j.Logger;

import javax.net.ServerSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

class ElevatedHandler {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	static final String CALLBACK_SECRET = "--callback-secret";

	static final String CALLBACK_PORT = "--callback-port";

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

		log.debug( "Sending task commands to elevated process..." );
		socket.getOutputStream().write( task.getOriginalLine().getBytes( TextUtil.CHARSET ) );
		socket.getOutputStream().write( '\n' );
		socket.getOutputStream().flush();
		log.debug( "  commands: " + task.getOriginalLine() );

		log.debug( "Reading task result from elevated process..." );
		String taskOutput = readLine( socket.getInputStream() );
		TaskResult result = TaskResult.parse( task, taskOutput );
		log.debug( "  result: " + result );

		return result;
	}

	private void waitForSocket() throws IOException, InterruptedException, TimeoutException {
		// Wait for the elevated process to get started
		int attemptCount = 0;
		int attemptLimit = 20;
		while( socket == null && attemptCount < attemptLimit && throwable == null ) {
			attemptCount++;
			wait( 1000 );
			log.trace( "Waiting for elevated process: " + attemptCount + " of " + attemptLimit + " seconds" );
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

		Parameters parameters = program.getParameters();

		if( parameters.isSet( LogFlag.LOG_FILE ) ) {
			processBuilder.command().add( LogFlag.LOG_FILE );
			processBuilder.command().add( parameters.get( LogFlag.LOG_FILE ).replace( ".log", "-elevated.log" ) );
		}
		if( parameters.isSet( LogFlag.LOG_LEVEL ) ) {
			processBuilder.command().add( LogFlag.LOG_LEVEL );
			processBuilder.command().add( parameters.get( LogFlag.LOG_LEVEL ) );
		}

		OperatingSystem.elevateProcessBuilder( program.getTitle(), processBuilder );
		log.debug( "Elevated commands: " + TextUtil.toString( processBuilder.command(), " " ) );
		Process process = processBuilder.start();
		new ProcessWatcherThread( process ).start();
	}

	private synchronized void setSocket( Socket socket ) {
		this.socket = socket;
		notifyAll();
	}

	private String readLine( InputStream input ) throws IOException {
		return new BufferedReader( new InputStreamReader( input, TextUtil.CHARSET ) ).readLine();
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
					if( readLine( peer.getInputStream() ).equals( secret ) ) {
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

		private Exception exception;

		public ProcessWatcherThread( Process process ) {
			this.process = process;
			setDaemon( true );
		}

		public void run() {
			try {
				process.waitFor();
				exitValue = process.exitValue();
				if( exitValue != 0 ) throw new IllegalStateException( "Elevated process failed to start" );
			} catch( Exception exception ) {
				throwable = exception;
			}
		}

		public int getExitValue() {
			return exitValue;
		}

	}

}
