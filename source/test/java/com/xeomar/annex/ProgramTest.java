package com.xeomar.annex;

import org.junit.Test;

import java.io.*;
import java.nio.charset.Charset;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ProgramTest {

	private static final String CHARSET = "utf-8";

	@Test( timeout = 2000 )
	public void testReadCommandsFromStdin() throws IOException {
		InputStream originalInput = System.in;
		PrintStream originalOutput = System.out;

		PipedInputStream outputPipe = new PipedInputStream();
		PipedOutputStream inputPipe = new PipedOutputStream();

		System.setIn( new PipedInputStream( inputPipe ) );
		System.setOut( new PrintStream( new PipedOutputStream( outputPipe ) ) );
		try {
			new Thread( () -> {
				try {
					new Program().run( new String[]{ UpdateFlag.STREAM } );
				} catch( IOException exception ) {
					exception.printStackTrace( System.err );
				}
			} ).start();

			inputPipe.write( "pause 0\n".getBytes( CHARSET ) );
			inputPipe.flush();
			TaskResult result1 = TaskResult.parse( new BufferedReader( new InputStreamReader( outputPipe, CHARSET ) ).readLine() );
			assertThat( result1.getStatus(), is( TaskStatus.SUCCESS ) );
			assertThat( result1.getMessage(), is( "success" ) );

			inputPipe.write( "pause 0".getBytes( CHARSET ) );
			inputPipe.close();
			TaskResult result2 = TaskResult.parse( new BufferedReader( new InputStreamReader( outputPipe, CHARSET ) ).readLine() );
			assertThat( result2.getStatus(), is( TaskStatus.SUCCESS ) );
			assertThat( result2.getMessage(), is( "success" ) );
		} finally {
			// Restore the original streams
			System.setOut( originalOutput );
			System.setIn( originalInput );
		}
	}

	@Test( timeout = 2000 )
	public void testReadCommandsFromBytes() throws IOException {
		PipedReader outputPipe = new PipedReader();
		PipedWriter inputPipe = new PipedWriter();

		PipedReader reader = new PipedReader( inputPipe );
		PipedWriter writer = new PipedWriter( outputPipe );

		new Thread( () -> {
			try {
				new Program().runTasksFromReader( reader, writer );
			} catch( IOException exception ) {
				exception.printStackTrace( System.err );
			}
		} ).start();

		inputPipe.write( "pause 0\n" );
		inputPipe.flush();
		TaskResult result1 = TaskResult.parse( new BufferedReader( outputPipe ).readLine() );
		assertThat( result1.getStatus(), is( TaskStatus.SUCCESS ) );
		assertThat( result1.getMessage(), is( "success" ) );

		inputPipe.write( "pause 0" );
		inputPipe.close();
		TaskResult result2 = TaskResult.parse( new BufferedReader( outputPipe ).readLine() );
		assertThat( result2.getStatus(), is( TaskStatus.SUCCESS ) );
		assertThat( result2.getMessage(), is( "success" ) );
	}

}