package com.xeomar.xevra.check;

import com.xeomar.xevra.Program;
import com.xeomar.xevra.UpdateFlag;
import com.xeomar.xevra.UpdateTask;
import com.xeomar.util.LogFlag;
import com.xeomar.util.ProcessCommands;
import com.xeomar.util.TextUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * This class is used to test elevated command execution.
 * <p>
 * It is not a unit test because it causes an elevated privileges dialog to be
 * displayed, requiring user input. It is also not in the test folder because
 * IntelliJ keys off the folder whether to run this as a module or a class. It
 * needs to be run from the java folder, not the test folder.
 */
public class ElevatedCommandCheck {

	public static void main( String[] commands ) {
		String modulePath = System.getProperty( "jdk.module.path" );
		String mainModule = Program.class.getModule().getName();
		String mainClass = Program.class.getName();

		ProcessBuilder processBuilder = new ProcessBuilder( ProcessCommands.forModule( null, modulePath, mainModule, mainClass ) );
		processBuilder.redirectError( ProcessBuilder.Redirect.INHERIT );
		processBuilder.command().add( UpdateFlag.STDIN );
		processBuilder.command().add( LogFlag.LOG_FILE );
		processBuilder.command().add( "privilege-check.log" );
		processBuilder.command().add( LogFlag.LOG_LEVEL );
		processBuilder.command().add( "info" );

		try {
			System.err.println( "Starting elevated process..." );
			Process process = processBuilder.start();
			process.getOutputStream().write( (UpdateTask.ELEVATED_ECHO + " hello\n").getBytes( TextUtil.CHARSET ) );
			process.getOutputStream().flush();
			System.err.println( "Elevated process started." );

			String result1 = new BufferedReader( new InputStreamReader( process.getInputStream(), TextUtil.CHARSET ) ).readLine();
			System.out.println( result1 );
		} catch( IOException exception ) {
			exception.printStackTrace();
		}

	}

}
