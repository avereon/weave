package com.xeomar.xevra;

import com.xeomar.util.OperatingSystem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class ElevatedProcessTest {

	private long wait = 100;

	private Program elevated;

	private ServerSocket server;

	private PrintWriter writer;

	private NonBlockingReader reader;

	@Before
	public void setup() throws Exception {
		System.setProperty( OperatingSystem.ELEVATED_PRIVILEGE_KEY, OperatingSystem.ELEVATED_PRIVILEGE_VALUE );

		// The interface to an elevated process is the socket
		// so to use it a server socket needs to be started

		String secret = UUID.randomUUID().toString();
		int port = 4324;

		server = new ServerSocket();
		server.setReuseAddress( true );
		server.bind( new InetSocketAddress( InetAddress.getLoopbackAddress(), port ) );

		elevated = new Program();
		elevated.start( ElevatedHandler.CALLBACK_SECRET, secret, ElevatedHandler.CALLBACK_PORT, String.valueOf( port ) );
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
		writer.println( "elevated-echo \"Hello Updater!\"\n" );
		writer.flush();
		assertThat( reader.readLine( wait, TimeUnit.MILLISECONDS ), is( "MESSAGE Hello Updater!" ) );
		assertThat( reader.readLine( wait, TimeUnit.MILLISECONDS ), is( "PROGRESS" ) );
		assertThat( reader.readLine( wait, TimeUnit.MILLISECONDS ), is( "SUCCESS echo Hello Updater!" ) );
		// There really is no further communication at this point,
		// any more reads will timeout and return a null
		assertThat( reader.readLine( wait, TimeUnit.MILLISECONDS ), is( nullValue() ) );
	}

}
