package com.xeomar.annex;

import com.xeomar.util.*;
import org.slf4j.Logger;

import javax.net.ssl.SSLServerSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeoutException;

class ElevatedHandler {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private Program program;

	private ServerSocket server;

	private Socket socket;

	private Throwable throwable;

	private String secret;

	ElevatedHandler( Program program ) {
		this.program = program;
	}

	public ElevatedHandler start() throws IOException {
		startServerSocket();
		startElevatedUpdater();
		return this;
	}

	public ElevatedHandler stop() throws IOException {
		if( socket != null ) socket.getOutputStream().close();
		return this;
	}

	private void startServerSocket() throws IOException {
		server = SSLServerSocketFactory.getDefault().createServerSocket();
		new ClientAcceptThread().start();
	}

	private void startElevatedUpdater() throws IOException {
		// TODO Finish implementation

		// NOTE IntelliJ keys off the folder whether to run this as a module
		// It needs to be run from the java folder, not the test folder
		ProcessBuilder processBuilder = new ProcessBuilder( ProcessCommands.forModule() );

		// TODO Need to send the callback port and secret
		processBuilder.command().add( "--callback-port" );
		processBuilder.command().add( String.valueOf( server.getLocalPort() ) );
		processBuilder.command().add( "--callback-secret" );
		processBuilder.command().add( secret );
		processBuilder.command().add( UpdateFlag.STREAM );

		Parameters parameters = program.getParameters();

		if( parameters.isSet( LogFlag.LOG_FILE ) ) {
			processBuilder.command().add( LogFlag.LOG_FILE );
			processBuilder.command().add( parameters.get( LogFlag.LOG_FILE ).replace( ".log", "-elevated.log" ) );
		}
		if( parameters.isSet( LogFlag.LOG_LEVEL ) ) {
			processBuilder.command().add( LogFlag.LOG_LEVEL );
			processBuilder.command().add( parameters.get( LogFlag.LOG_LEVEL ) );
		}

		//					File home = new File( System.getProperty( "user.home" ));
		//					File logFile = new File( parameters.get( LogFlag.LOG_FILE ).replace( "%h", home.toString() ).replace( ".log", "-mvs.log" ) );
		//					log.info( "MVS log file: " + logFile );
		//					processBuilder.redirectOutput( ProcessBuilder.Redirect.to( logFile ) ).redirectError( ProcessBuilder.Redirect.to( logFile ) );

		//processBuilder.redirectError( ProcessBuilder.Redirect.INHERIT );
		OperatingSystem.elevateProcessBuilder( program.getTitle(), processBuilder );
		log.info( "Elevated commands: " + TextUtil.toString( processBuilder.command(), " " ) );
		processBuilder.start();
	}

	public synchronized TaskResult execute( AnnexTask task ) throws IOException, InterruptedException, TimeoutException {
		int attempt = 0;
		int attemptCount = 5;
		while( socket == null && attempt < attemptCount ) {
			wait( 20 );
			attemptCount++;
		}
		if( attempt > attemptCount ) throw new TimeoutException( "Timeout waiting for elevated updater to start" );

		log.debug( "Sending task commands to elevated process..." );
		log.debug( "  commands: " + task.getOriginalLine() );

		socket.getOutputStream().write( task.getOriginalLine().getBytes( TextUtil.CHARSET ) );
		socket.getOutputStream().write( '\n' );
		socket.getOutputStream().flush();

		log.debug( "Reading task result from elevated process..." );
		String taskOutput = readLine( socket.getInputStream() );
		log.debug( "Task output: " + taskOutput );
		TaskResult result = TaskResult.parse( task, taskOutput );
		log.debug( "  result: " + result );

		return result;
	}

	private synchronized void setSocket( Socket socket ) {
		this.socket = socket;
		notifyAll();
	}

	private String readLine( InputStream input ) throws IOException {
		return new BufferedReader( new InputStreamReader( input, TextUtil.CHARSET ) ).readLine();
	}

	private class ClientAcceptThread extends Thread {

		@Override
		public void run() {
			try {
				while( socket == null ) {
					Socket client = server.accept();
					if( readLine( client.getInputStream() ).equals( secret ) ) setSocket( client );
				}
			} catch( Throwable throwable ) {
				ElevatedHandler.this.throwable = throwable;
			}
		}

	}

}
