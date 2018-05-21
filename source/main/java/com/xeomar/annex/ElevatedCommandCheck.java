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
		processBuilder.command().add( "trace" );

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

}
