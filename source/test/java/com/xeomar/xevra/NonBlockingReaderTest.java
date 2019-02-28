package com.xeomar.xevra;

import com.xeomar.util.ThreadUtil;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class NonBlockingReaderTest {

	@Test
	public void testReadLine() throws Exception {
		String line1 = "I am first with five";
		String line2 = "Then seven in the middle";
		String line3 = "Five again to end";

		String content = line1 + "\n" + line2 + "\n" + line3;
		NonBlockingReader reader = new NonBlockingReader( new StringReader( content ) );

		assertThat( reader.readLine( 100, TimeUnit.MILLISECONDS ), is( line1 ) );
		assertThat( reader.readLine( 100, TimeUnit.MILLISECONDS ), is( line2 ) );
		assertThat( reader.readLine( 100, TimeUnit.MILLISECONDS ), is( line3 ) );
		assertThat( reader.readLine( 100, TimeUnit.MILLISECONDS ), is( nullValue() ) );
	}

	@Test
	public void testReadLineWithTimeout() throws Exception {
		try {
			NonBlockingReader reader = null;
			try {
				reader = new NonBlockingReader( new InputStreamReader( System.in ) );
				assertThat( reader.readLine( 100, TimeUnit.MILLISECONDS ), is( nullValue() ) );
			} finally {
				if( reader != null ) reader.close();
			}
		} catch( java.io.IOException exception ) {
			exception.printStackTrace();
		}
	}

	@Test
	public void testClose() throws Exception {
		 NonBlockingReader reader = new NonBlockingReader( new InputStreamReader( System.in ) );

		new Thread( () -> {
			System.out.println("Waiting a bit..." );
			ThreadUtil.pause( 1000 );
			try {
				reader.close();
			} catch( IOException exception ) {
				exception.printStackTrace();
			}
			System.out.println( "Reader is closed!" );
		} ).start();

		try {
			reader.readLine( 2000, TimeUnit.MILLISECONDS );
			fail( "readLine() should have thrown an exception" );
		} catch( InterruptedException exception ) {
			// Intentionally ignore exception
		}
	}

}
