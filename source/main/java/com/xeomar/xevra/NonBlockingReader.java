package com.xeomar.xevra;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class NonBlockingReader {

	private final BlockingQueue<String> lines = new LinkedBlockingQueue<>();

	private Thread readerThread;

	private BufferedReader source;

	private IOException exception;

	private InterruptedException interruptedException;

	private boolean closed;

	public NonBlockingReader( Reader reader ) {
		if( reader == null ) throw new NullPointerException( "Reader cannot be null" );
		this.source = (reader instanceof BufferedReader ? (BufferedReader)reader : new BufferedReader( reader ));
		readerThread = new ReaderTask();
		readerThread.start();
	}

	public String readLine( long time, TimeUnit unit ) throws IOException, InterruptedException {
		String line = closed && lines.size() == 0 ? null : lines.poll( time, unit );
		if( this.interruptedException != null ) throw this.interruptedException;
		if( this.exception != null ) throw this.exception;
		return line;
	}

	public void close() throws IOException {
		if( readerThread != null ) readerThread.interrupt();
		readerThread = null;
		//source.close();
	}

	private class ReaderTask extends Thread {

		public ReaderTask() {
			super( "NonBlockingReaderThread" );
			setDaemon( true );
		}

		public void run() {
			try {
				while( !isInterrupted() ) {
					System.out.println( "Reading a line..." );
					String line = source.readLine();
					System.out.println( "Thread interrupted: " + isInterrupted() );
					if( isInterrupted() ) NonBlockingReader.this.interruptedException = new InterruptedException();
					if( line == null ) break;
					lines.add( line );
				}
			} catch( IOException exception ) {
				NonBlockingReader.this.exception = exception;
			} finally {
				NonBlockingReader.this.closed = true;
				if( source != null ) {
					try {
						source.close();
					} catch( IOException exception ) {
						NonBlockingReader.this.exception = exception;
					}
				}
			}
		}

	}

}
