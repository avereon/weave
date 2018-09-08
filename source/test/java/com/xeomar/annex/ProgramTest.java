package com.xeomar.annex;

import com.xeomar.product.ProductCard;
import com.xeomar.util.LogFlag;
import com.xeomar.util.LogUtil;
import com.xeomar.util.Parameters;
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
					new Program().start( new String[]{ UpdateFlag.STDIN, LogFlag.LOG_LEVEL, "none" } );
				} catch( Exception exception ) {
					exception.printStackTrace( System.err );
				}
			} ).start();

			inputPipe.write( "pause 13\n".getBytes( CHARSET ) );
			inputPipe.write( "pause 7\n".getBytes( CHARSET ) );
			inputPipe.close();
			String result1 = new BufferedReader( new InputStreamReader( outputPipe, CHARSET ) ).readLine();
			assertThat( result1, is( "SUCCESS paused 13ms" ) );
			String result2 = new BufferedReader( new InputStreamReader( outputPipe, CHARSET ) ).readLine();
			assertThat( result2, is( "SUCCESS paused 7ms" ) );
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
				Program program = new Program();
				LogUtil.configureLogging( program, Parameters.parse( LogFlag.LOG_LEVEL, "none" ) );
				program.runTasksFromReader( reader, writer );
			} catch( Exception exception ) {
				exception.printStackTrace( System.err );
			}
		} ).start();

		inputPipe.write( "pause 17\n" );
		inputPipe.write( "pause 5" );
		inputPipe.close();

		String result1 = new BufferedReader( outputPipe ).readLine();
		assertThat( result1, is( "SUCCESS paused 17ms" ) );

		String result2 = new BufferedReader( outputPipe ).readLine();
		assertThat( result2, is( "SUCCESS paused 5ms" ) );
	}

}
