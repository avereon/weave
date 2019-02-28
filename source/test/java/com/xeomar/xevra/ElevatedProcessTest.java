package com.xeomar.xevra;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

public class ElevatedProcessTest {

	private Program elevated;

	@Before
	public void setup() throws Exception {
		// The interface to an elevated process is the socket
		// so to use it a server socket needs to be started

		String secret = UUID.randomUUID().toString();
		int port = 4324;

		ServerSocket server = new ServerSocket( port );

		elevated = new Program();
		elevated.start( ElevatedHandler.CALLBACK_SECRET, secret, ElevatedHandler.CALLBACK_PORT, String.valueOf( port ) );
		elevated.waitForStart();

		Socket socket = server.accept();
		BufferedReader reader = new BufferedReader( new InputStreamReader(socket.getInputStream()) );
		assertThat( secret, is( reader.readLine() ));
		//System.out.println( reader.readLine() );


	}

	@Test
	public void testStartupShutdown() {

	}

}
