package com.avereon.zenna.check;

import com.avereon.util.LogFlag;
import com.avereon.util.ProcessCommands;
import com.avereon.util.TextUtil;
import com.avereon.zenna.Program;
import com.avereon.zenna.UpdateFlag;
import com.avereon.zenna.UpdateTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class ElevatedCommandCheck {

	public static void main( String[] commands ) {
		new ElevatedCommandCheck().run();
	}

	public List<String> getAdditionalCommands() {
		return List.of();
	}

	public void run() {
		String modulePath = System.getProperty( "jdk.module.path" );
		String mainModule = Program.class.getModule().getName();
		String mainClass = Program.class.getName();

		ProcessBuilder processBuilder = new ProcessBuilder( ProcessCommands.forModule( null, modulePath, mainModule, mainClass ) );
		processBuilder.redirectError( ProcessBuilder.Redirect.INHERIT );
		processBuilder.command().add( UpdateFlag.STDIN );
		processBuilder.command().add( LogFlag.LOG_FILE );
		processBuilder.command().add( "target/privilege-check.log" );
		processBuilder.command().add( LogFlag.LOG_LEVEL );
		processBuilder.command().add( "debug" );
		processBuilder.command().addAll( getAdditionalCommands() );

		try {
			Process process = processBuilder.start();
			process.getOutputStream().write( (UpdateTask.PAUSE + " 500 \"Preparing update\"\n").getBytes( TextUtil.CHARSET ) );
			process.getOutputStream().write( (UpdateTask.ELEVATED_ECHO + " hello1\n").getBytes( TextUtil.CHARSET ) );
			process.getOutputStream().write( (UpdateTask.ELEVATED_PAUSE + " 2000 \"Simulating update\"\n").getBytes( TextUtil.CHARSET ) );
			process.getOutputStream().write( (UpdateTask.ELEVATED_ECHO + " hello2\n").getBytes( TextUtil.CHARSET ) );
			process.getOutputStream().write( (UpdateTask.PAUSE + " 500 \"Finishing update\"\n").getBytes( TextUtil.CHARSET ) );
			process.getOutputStream().close();

			check( "SUCCESS pause paused 500ms", readLine( process.getInputStream() ) );
			check( "SUCCESS echo hello1", readLine( process.getInputStream() ) );
			check( "SUCCESS pause paused 2000ms", readLine( process.getInputStream() ) );
			check( "SUCCESS echo hello2", readLine( process.getInputStream() ) );
			check( "SUCCESS pause paused 500ms", readLine( process.getInputStream() ) );
		} catch( IOException exception ) {
			exception.printStackTrace();
		}
	}

	private String readLine( InputStream input ) throws IOException {
		return new BufferedReader( new InputStreamReader( input, TextUtil.CHARSET ) ).readLine();
	}

	private void check( String expected, String result ) {
		if( !expected.equals( result ) ) throw new RuntimeException( "Unexpected result\nexpected: " + expected + "\n     was: " + result );
	}

}
