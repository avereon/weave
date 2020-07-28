package com.avereon.weave;

import com.avereon.product.ProductCard;
import com.avereon.util.LogFlag;
import com.avereon.util.Log;
import com.avereon.util.Parameters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ProgramTest {

	private static final String CHARSET = "utf-8";

	@Test
	public void testProductCard() {
		ProductCard card = new Program().getCard();
		assertThat( card, not( is( nullValue() ) ) );
	}

	@Test
	@Timeout( 2 )
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
					new Program().start( UpdateFlag.STDIN, LogFlag.LOG_LEVEL, "none" );
				} catch( Exception exception ) {
					exception.printStackTrace( System.err );
				}
			} ).start();

			inputPipe.write( "pause 130\n".getBytes( CHARSET ) );
			inputPipe.write( "pause 170\n".getBytes( CHARSET ) );
			inputPipe.close();

			String result1 = new BufferedReader( new InputStreamReader( outputPipe, CHARSET ) ).readLine();
			assertThat( result1, is( "SUCCESS pause 130" ) );
			String result2 = new BufferedReader( new InputStreamReader( outputPipe, CHARSET ) ).readLine();
			assertThat( result2, is( "SUCCESS pause 170" ) );
		} finally {
			// Restore the original streams
			System.setOut( originalOutput );
			System.setIn( originalInput );
		}
	}

	@Test
	@Timeout( 2 )
	public void testReadCommandsFromBytes() throws IOException {
		PipedReader outputPipe = new PipedReader();
		PipedWriter inputPipe = new PipedWriter();

		PipedReader reader = new PipedReader( inputPipe );
		PipedWriter writer = new PipedWriter( outputPipe );

		new Thread( () -> {
			try {
				Program program = new Program();
				Log.configureLogging( program, Parameters.parse( LogFlag.LOG_LEVEL, "none" ) );
				program.runTasksFromReader( reader, writer );
			} catch( Exception exception ) {
				exception.printStackTrace( System.err );
			}
		} ).start();

		inputPipe.write( "pause 170\n" );
		inputPipe.write( "pause 190" );
		inputPipe.close();

		String result1 = new BufferedReader( outputPipe ).readLine();
		assertThat( result1, is( "SUCCESS pause 170" ) );
		String result2 = new BufferedReader( outputPipe ).readLine();
		assertThat( result2, is( "SUCCESS pause 190" ) );
	}

}
