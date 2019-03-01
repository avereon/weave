package com.xeomar.xevra;

import com.xeomar.util.ThreadUtil;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class NonBlockingReaderTest {

	private static long delay = 50;

	@Test
	public void testReadLine() throws Exception {
		String line1 = "I am first with five";
		String line2 = "Then seven in the middle";
		String line3 = "Five again to end";

		String content = line1 + "\n" + line2 + "\n" + line3;
		NonBlockingReader reader = new NonBlockingReader( new StringReader( content ) );

		long time = delay;
		assertThat( reader.readLine( time, TimeUnit.MILLISECONDS ), is( line1 ) );
		assertThat( reader.readLine( time, TimeUnit.MILLISECONDS ), is( line2 ) );
		assertThat( reader.readLine( time, TimeUnit.MILLISECONDS ), is( line3 ) );
		assertThat( reader.readLine( time, TimeUnit.MILLISECONDS ), is( nullValue() ) );
	}

	@Test
	public void testReadLineWithTimeout() throws Exception {
		try {
			NonBlockingReader reader = null;
			try {
				reader = new NonBlockingReader( System.in );
				assertThat( reader.readLine( delay, TimeUnit.MILLISECONDS ), is( nullValue() ) );
			} finally {
				if( reader != null ) reader.close();
			}
		} catch( java.io.IOException exception ) {
			exception.printStackTrace();
		}
	}

	@Test
	public void testClose() throws Exception {
		long time = delay;

		NonBlockingReader reader = new NonBlockingReader( System.in );

		// Setup a thread that will close the reader before the read times out
		new Thread( () -> {
			ThreadUtil.pause( time );
			try {
				reader.close();
			} catch( IOException exception ) {
				exception.printStackTrace();
			}
		} ).start();

		// Read a line with a timeout longer than the closing thread pause
		assertThat( reader.readLine( 2 * time, TimeUnit.MILLISECONDS ), is( nullValue() ) );
	}

	@Test
	public void testIntermittentSource() throws Exception {
		StringBuilder builder = new StringBuilder();
		for( int index = 0; index < 100; index++ ) {
			builder.append( "Line " ).append( index ).append( "\n" );
		}

		NonBlockingReader reader = new NonBlockingReader( new IntermittentReader( new StringReader( builder.toString().trim() ) ) );

		long time = 1000;
		for( int index = 0; index < 100; index++ ) {
			ThreadUtil.pause( 800 );
			String line = reader.readLine( time, TimeUnit.MILLISECONDS );
			System.err.println( "line: " + line );
			assertThat( line, is( "Line " + index ) );
		}

		// Setup a thread that will close the reader before the read times out
		//		new Thread( () -> {
		//			try {
		//				reader.close();
		//			} catch( IOException exception ) {
		//				exception.printStackTrace();
		//			}
		//		} ).start();

		// Read a line with a timeout longer than the closing thread pause
		//assertThat( reader.readLine( 2 * time, TimeUnit.MILLISECONDS ), is( nullValue() ) );
	}

	private class IntermittentReader extends BufferedReader {

		IntermittentReader( Reader source ) {
			super(source);
		}

		@Override
		public String readLine() throws IOException {
			return super.readLine();
		}
	}

}
