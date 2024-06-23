package com.avereon.weave;

import com.avereon.util.LogFlag;
import com.avereon.util.NonBlockingReader;
import com.avereon.util.OperatingSystem;
import com.avereon.weave.task.LaunchTask;
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

	private Weave elevated;

	private ServerSocket server;

	private PrintWriter writer;

	private NonBlockingReader reader;

	@BeforeEach
	public void setup() throws Exception {
		// NOTE The term "elevated" is in quotes, meaning that the test is emulating an elevated process.

		// Convince the OperatingSystem class that the process is "elevated"
		System.setProperty( OperatingSystem.PROCESS_PRIVILEGE_KEY, OperatingSystem.ELEVATED_PRIVILEGE_VALUE );

		String secret = UUID.randomUUID().toString();
		int port = 4324;

		// The interface to an "elevated" process is the socket
		// so to use it a server socket needs to be started
		server = new ServerSocket();
		server.setReuseAddress( true );
		server.bind( new InetSocketAddress( InetAddress.getLoopbackAddress(), port ) );

		elevated = new Weave();
		elevated.start( ElevatedFlag.CALLBACK_SECRET, secret, ElevatedFlag.CALLBACK_PORT, String.valueOf( port ), LogFlag.LOG_LEVEL, LogFlag.NONE );
		elevated.waitForStart( 1, TimeUnit.SECONDS );

		Socket socket = server.accept();
		writer = new PrintWriter( socket.getOutputStream(), false, StandardCharsets.UTF_8 );
		reader = new NonBlockingReader( new InputStreamReader( socket.getInputStream(), StandardCharsets.UTF_8 ) );

		// Check the "elevated" weave secret
		assertThat( secret ).isEqualTo( reader.readLine( wait, TimeUnit.MILLISECONDS ) );

		// The "elevated" updater should be running and validated at this point
		assertThat( elevated.getStatus() ).isEqualTo( Weave.Status.STARTED );
	}

	@AfterEach
	public void shutdown() throws Exception {
		elevated.stop();
		elevated.waitForStop( 1, TimeUnit.SECONDS );
		assertThat( elevated.getStatus() ).isEqualTo( Weave.Status.STOPPED );
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
		// any more reads will time out and return a null
		assertThat( readNext() ).isNull();
	}

	@Test
	public void testElevatedExecuteSuccess() throws Exception {
		writer.println( UpdateTask.EXECUTE + " " + workingFolder + " java" );
		writer.flush();
		assertThat( readNext( 2 * wait, TimeUnit.MILLISECONDS ) ).isEqualTo( "MESSAGE Executing java" );
		assertThat( readNext() ).isEqualTo( "PROGRESS" );
		assertThat( readNext() ).isEqualTo( "SUCCESS execute java" );
		assertThat( readNext() ).isNull();
	}

	@Test
	public void testElevatedExecuteFailure() throws Exception {
		writer.println( UpdateTask.EXECUTE + " " + workingFolder + " invalid" );
		writer.flush();
		assertThat( readNext( 2 * wait, TimeUnit.MILLISECONDS ) ).isEqualTo( "MESSAGE Executing invalid" );
		assertThat( readNext() ).startsWith( "FAILURE execute IOException: Cannot run program \"invalid\"" );
		assertThat( readNext() ).isNull();
	}

	@Test
	public void testElevatedLaunchSuccess() throws Exception {
		writer.println( UpdateTask.LAUNCH + " " + workingFolder + " java" );
		writer.flush();
		assertThat( readNext( 2 * wait, TimeUnit.MILLISECONDS ) ).isEqualTo( "MESSAGE Launching java" );
		assertThat( readNext() ).isEqualTo( "PROGRESS" );
		assertThat( readNext() ).isEqualTo( "SUCCESS launch java" );
		assertThat( readNext() ).isNull();
	}

	@Test
	public void testElevatedLaunchFailure() throws Exception {
		writer.println( UpdateTask.LAUNCH + " " + workingFolder + " invalid" );
		writer.flush();
		assertThat( readNext( 2 * wait + LaunchTask.TIMEOUT + LaunchTask.WAIT, TimeUnit.MILLISECONDS ) ).isEqualTo( "MESSAGE Launching invalid" );
		assertThat( readNext() ).startsWith( "FAILURE launch IOException: Cannot run program \"invalid\"" );
		assertThat( readNext() ).isNull();
	}

	private String readNext() throws IOException {
		return readNext( wait, TimeUnit.MILLISECONDS );
	}

	private String readNext( long duration, TimeUnit unit ) throws IOException {
		String line;
		while( (line = reader.readLine( duration, unit )) != null ) {
			if( !line.startsWith( ElevatedHandler.LOG ) ) return line;
		}
		return null;
	}

}
