package com.xeomar.annex;

import com.xeomar.product.ProductCard;
import org.junit.Test;

import java.io.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ProgramTest {

	private static final String CHARSET = "utf-8";

	@Test
	public void testProductCard() {
		ProductCard card = new Program().getCard();
		assertThat( card, not( is( nullValue() ) ) );
	}

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
			String result1 = new BufferedReader( new InputStreamReader( outputPipe, CHARSET ) ).readLine();
			assertThat( result1, is( "SUCCESS success" ) );

			inputPipe.write( "pause 0\n".getBytes( CHARSET ) );
			inputPipe.close();
			String result2 = new BufferedReader( new InputStreamReader( outputPipe, CHARSET ) ).readLine();
			assertThat( result2, is( "SUCCESS success" ) );
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
		String result1 = new BufferedReader( outputPipe ).readLine();
		assertThat( result1, is( "SUCCESS success" ) );

		inputPipe.write( "pause 0" );
		inputPipe.close();
		String result2 = new BufferedReader( outputPipe ).readLine();
		assertThat( result2, is( "SUCCESS success" ) );
	}

}
