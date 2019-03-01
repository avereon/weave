package com.xeomar.xevra;

import com.xeomar.util.ThreadUtil;
import org.junit.Test;

import java.io.IOException;
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

}
