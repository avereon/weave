package com.xeomar.xevra;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class NonBlockingReader {

	private final BlockingQueue<String> lines = new LinkedBlockingQueue<>();

	private Thread callerThread;

	private Thread readerThread;

	private BufferedReader source;

	private IOException ioexception;

	private boolean closed;

	public NonBlockingReader( Reader reader ) {
		if( reader == null ) throw new NullPointerException( "Reader cannot be null" );
		source = (reader instanceof BufferedReader ? (BufferedReader)reader : new BufferedReader( reader ));
		readerThread = new ReaderTask();
		readerThread.start();
	}

	public String readLine( long time, TimeUnit unit ) throws IOException, InterruptedException {
		this.callerThread = Thread.currentThread();
		if( closed && lines.size() == 0 ) return null;
		try {
			return lines.poll( time, unit );
		} catch( InterruptedException exception ) {
			if( ioexception != null ) throw ioexception;
			if( closed ) return null;
			throw exception;
		}
	}

	public void close() throws IOException {
		closed = true;
		//if( callerThread != null ) callerThread.interrupt();
		if( readerThread != null ) readerThread.interrupt();
		if( ioexception != null ) throw ioexception;
		readerThread = null;
	}

	private class ReaderTask extends Thread {

		ReaderTask() {
			super( "NonBlockingReaderThread" );
			setDaemon( true );
		}

		public void run() {
			try {
				while( !interrupted() ) {
					String line = source.readLine();
					if( line == null ) return;
					lines.add( line );
				}
			} catch( IOException exception ) {
				ioexception = exception;
			} finally {
				//callerThread.interrupt();
				closed = true;
			}
		}

	}

}
