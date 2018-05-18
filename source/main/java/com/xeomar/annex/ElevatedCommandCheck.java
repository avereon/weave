package com.xeomar.annex;

import com.xeomar.util.LogFlag;
import com.xeomar.util.ProcessCommands;
import com.xeomar.util.TextUtil;

import java.io.*;

public class ElevatedCommandCheck {

	public static void main( String[] commands ) {
		String path = System.getProperty( "jdk.module.path" );
		String mainModule = Program.class.getModule().getName();
		String mainClass = Program.class.getName();

		ProcessBuilder processBuilder = new ProcessBuilder( ProcessCommands.forModule( path, mainModule, mainClass ) );
		processBuilder.command().add( UpdateFlag.STREAM );
		processBuilder.command().add( LogFlag.LOG_FILE );
		processBuilder.command().add( "privilege-check.log" );
		processBuilder.command().add( LogFlag.LOG_LEVEL );
		processBuilder.command().add( "debug" );

		try {
			Process process = processBuilder.start();
			process.getOutputStream().write( (UpdateTask.ELEVATED_ECHO + " hello\n").getBytes( TextUtil.CHARSET ) );
			process.getOutputStream().flush();

			String result1 = new BufferedReader( new InputStreamReader( process.getInputStream(), TextUtil.CHARSET ) ).readLine();
			System.out.println( result1 );
		} catch( IOException e ) {
			e.printStackTrace();
		}

	}

	public static void oldmain( String[] commands ) {
		InputStream originalInput = System.in;
		PrintStream originalOutput = System.out;

		try {
			PipedInputStream outputPipe = new PipedInputStream();
			PipedOutputStream inputPipe = new PipedOutputStream();

			System.setIn( new PipedInputStream( inputPipe ) );
			System.setOut( new PrintStream( new PipedOutputStream( outputPipe ) ) );
			new Thread( () -> {
				try {
					new Program().run( new String[]{ UpdateFlag.STREAM, LogFlag.LOG_FILE, "privilege-check.log", LogFlag.LOG_LEVEL, "debug" } );
				} catch( IOException exception ) {
					exception.printStackTrace( System.err );
				}
			} ).start();

			inputPipe.write( (UpdateTask.ELEVATED_ECHO + " hello\n").getBytes( TextUtil.CHARSET ) );
			inputPipe.flush();
			String result1 = new BufferedReader( new InputStreamReader( outputPipe, TextUtil.CHARSET ) ).readLine();

			//			inputPipe.write( "pause 0\n".getBytes( TextUtil.CHARSET ) );
			//			inputPipe.close();
			//			String result2 = new BufferedReader( new InputStreamReader( outputPipe, TextUtil.CHARSET ) ).readLine();
		} catch( Exception exception ) {
			exception.printStackTrace( System.err );
		} finally {
			// Restore the original streams
			System.setOut( originalOutput );
			System.setIn( originalInput );
		}
	}

}
