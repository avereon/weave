package com.avereon.weave;

import com.avereon.util.LogFlag;
import com.avereon.util.NonBlockingReader;
import com.avereon.util.OperatingSystem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class ElevatedProcessTest {

	private static final String workingFolder = System.getProperty( "user.dir" );

	private static final long wait = 100;

	private Program elevated;

	private ServerSocket server;

	private PrintWriter writer;

	private NonBlockingReader reader;

	@BeforeEach
	public void setup() throws Exception {
		// Convince the OperatingSystem class that the process is elevated
		System.setProperty( OperatingSystem.PROCESS_PRIVILEGE_KEY, OperatingSystem.ELEVATED_PRIVILEGE_VALUE );

		// The interface to an elevated process is the socket
		// so to use it a server socket needs to be started

		String secret = UUID.randomUUID().toString();
		int port = 4324;

		server = new ServerSocket();
		server.setReuseAddress( true );
		server.bind( new InetSocketAddress( InetAddress.getLoopbackAddress(), port ) );

		elevated = new Program();
		elevated.start( ElevatedFlag.CALLBACK_SECRET, secret, ElevatedFlag.CALLBACK_PORT, String.valueOf( port ), LogFlag.LOG_LEVEL, "none" );
		elevated.waitForStart( 1, TimeUnit.SECONDS );

		Socket socket = server.accept();
		writer = new PrintWriter( socket.getOutputStream(), false, StandardCharsets.UTF_8 );
		reader = new NonBlockingReader( new InputStreamReader( socket.getInputStream(), StandardCharsets.UTF_8 ) );
		assertThat( secret ).isEqualTo( reader.readLine( wait, TimeUnit.MILLISECONDS ) );

		// The elevated updater should be running and validated at this point
		assertThat( elevated.getStatus() ).isEqualTo( Program.Status.STARTED );
	}

	@AfterEach
	public void shutdown() throws Exception {
		elevated.stop();
		elevated.waitForStop( 1, TimeUnit.SECONDS );
		assertThat( elevated.getStatus() ).isEqualTo( Program.Status.STOPPED );
		server.close();
		System.setProperty( OperatingSystem.PROCESS_PRIVILEGE_KEY, OperatingSystem.NORMAL_PRIVILEGE_VALUE );
	}

	@Test
	public void testStartupShutdown() {
		// This test makes sure the setup and shutdown methods work without and other work
	}

	@Test
	public void testElevatedLog() throws Exception {
		writer.println( "elevated-log \"Hello Updater!\"" );
		writer.flush();
		assertThat( readNext() ).isEqualTo( "PROGRESS" );
		assertThat( readNext() ).isEqualTo( "SUCCESS log \"Hello Updater!\"" );
		// There really is no further communication at this point,
		// any more reads will timeout and return a null
		assertThat( readNext() ).isNull();
	}

	@Test
	public void testLaunchSuccess() throws Exception {
		writer.println( UpdateTask.LAUNCH + " " + workingFolder + " java" );
		writer.flush();
		assertThat( readNext() ).isEqualTo( "MESSAGE Launching java" );
		assertThat( readNext() ).isEqualTo( "PROGRESS" );
		assertThat( readNext() ).isEqualTo( "SUCCESS launch java" );
		assertThat( readNext() ).isNull();
	}

	@Test
	public void testLaunchFailure() throws Exception {
		writer.println( UpdateTask.LAUNCH + " " + workingFolder + " invalid" );
		writer.flush();
		assertThat( readNext() ).isEqualTo( "MESSAGE Launching invalid" );
		assertThat( readNext() ).startsWith( "FAILURE launch IOException: Cannot run program \"invalid\"" );
		assertThat( readNext() ).isNull();
	}

	private String readNext() throws IOException {
		String line;
		while( (line = reader.readLine( wait, TimeUnit.MILLISECONDS )) != null ) {
			if( !line.startsWith( ElevatedHandler.LOG ) ) return line;
		}
		return null;
	}

}
