package com.avereon.zenna;

import com.avereon.util.LogFlag;
import com.avereon.util.NonBlockingReader;
import com.avereon.util.OperatingSystem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ElevatedProcessTest {

	private long wait = 100;

	private Program elevated;

	private ServerSocket server;

	private PrintWriter writer;

	private NonBlockingReader reader;

	private String workingFolder = System.getProperty( "user.dir" );

	@Before
	public void setup() throws Exception {
		// Convince the OperatingSystem class that the process is elevated
		System.setProperty( OperatingSystem.ELEVATED_PRIVILEGE_KEY, OperatingSystem.ELEVATED_PRIVILEGE_VALUE );

		// The interface to an elevated process is the socket
		// so to use it a server socket needs to be started

		String secret = UUID.randomUUID().toString();
		int port = 4324;

		server = new ServerSocket();
		server.setReuseAddress( true );
		server.bind( new InetSocketAddress( InetAddress.getLoopbackAddress(), port ) );

		elevated = new Program();
		elevated.start( ElevatedHandler.CALLBACK_SECRET, secret, ElevatedHandler.CALLBACK_PORT, String.valueOf( port ), LogFlag.LOG_LEVEL, "none" );
		elevated.waitForStart( 1, TimeUnit.SECONDS );

		Socket socket = server.accept();
		writer = new PrintWriter( socket.getOutputStream(), false, StandardCharsets.UTF_8 );
		reader = new NonBlockingReader( new InputStreamReader( socket.getInputStream(), StandardCharsets.UTF_8 ) );
		assertThat( secret, is( reader.readLine( wait, TimeUnit.MILLISECONDS ) ) );

		// The elevated updater should be running and validated at this point
		assertThat( elevated.getStatus(), is( Program.Status.STARTED ) );
	}

	@After
	public void shutdown() throws Exception {
		elevated.stop();
		elevated.waitForStop( 1, TimeUnit.SECONDS );
		assertThat( elevated.getStatus(), is( Program.Status.STOPPED ) );
		server.close();
		System.setProperty( OperatingSystem.ELEVATED_PRIVILEGE_KEY, OperatingSystem.NORMAL_PRIVILEGE_VALUE );
	}

	@Test
	public void testStartupShutdown() {
		// This test makes sure the setup and shutdown methods work without and other work
	}

	@Test
	public void testElevatedEcho() throws Exception {
		writer.println( "elevated-echo \"Hello Updater!\"" );
		writer.flush();
		assertThat( readNext(), is( "MESSAGE Hello Updater!" ) );
		assertThat( readNext(), is( "PROGRESS" ) );
		assertThat( readNext(), is( "SUCCESS echo Hello Updater!" ) );
		// There really is no further communication at this point,
		// any more reads will timeout and return a null
		assertThat( readNext(), is( nullValue() ) );
	}

	@Test
	public void testLaunchSuccess() throws Exception {
		writer.println( UpdateTask.LAUNCH + " " + workingFolder + " java" );
		writer.flush();
		assertThat( readNext(), is( "MESSAGE Launching java" ) );
		assertThat( readNext(), is( "PROGRESS" ) );
		assertThat( readNext(), is( "SUCCESS launch java" ) );
		assertThat( readNext(), is( nullValue() ) );
	}

	@Test
	public void testLaunchFailure() throws Exception {
		writer.println( UpdateTask.LAUNCH + " " + workingFolder + " invalid" );
		writer.flush();
		assertThat( readNext(), is( "MESSAGE Launching invalid" ) );
		assertThat( readNext(), startsWith( "FAILURE launch IOException: Cannot run program \"invalid\"" ) );
		assertThat( readNext(), is( nullValue() ) );
	}

	private String readNext() throws IOException {
		String line;
		while( (line = reader.readLine( wait, TimeUnit.MILLISECONDS )) != null ) {
			if( !line.startsWith( ElevatedHandler.LOG ) ) return line;
		}
		return null;
	}

}
