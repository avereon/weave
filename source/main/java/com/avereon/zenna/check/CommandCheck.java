package com.avereon.zenna.check;

import com.avereon.util.LogFlag;
import com.avereon.util.ProcessCommands;
import com.avereon.util.TextUtil;
import com.avereon.zenna.Program;
import com.avereon.zenna.UpdateFlag;
import com.avereon.zenna.UpdateTask;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CommandCheck {

	public static void main( String[] commands ) {
		new CommandCheck().run();
	}

	public List<String> getProgramCommands() {
		List<String> commands = new ArrayList<>();
		commands.add( UpdateFlag.STDIN );
		commands.add( LogFlag.LOG_FILE );
		commands.add( getLogFile() );
		commands.add( LogFlag.LOG_LEVEL );
		commands.add( LogFlag.DEBUG );
		return commands;
	}

	protected String getLogFile() {
		return "target/check.log";
	}

	public List<String> getProcessCommands() {
		List<String> commands = new ArrayList<>();
		commands.add( UpdateTask.LOG + " \"starting updates...\"\n" );

		int steps = 10;
		for( int groupIndex = 0; groupIndex < 5; groupIndex++ ) {
			commands.add( UpdateTask.HEADER + " \"Updating Product " + (char)(groupIndex + 65) + "\"\n" );
			for( int index = 0; index < steps; index++ ) {
				commands.add( UpdateTask.PAUSE + " 100 \"update step " + index + "\"\n" );
			}
			steps = 2;
		}

		commands.add( UpdateTask.LOG + " \"updates complete\"\n" );
		return commands;
	}

	public void check( Process process ) throws Exception {
		check( "SUCCESS log \"starting updates...\"", readLine( process.getInputStream() ) );

		int steps = 10;
		for( int groupIndex = 0; groupIndex < 5; groupIndex++ ) {
			check( "SUCCESS " + UpdateTask.HEADER + " \"Updating Product " + (char)(groupIndex + 65) + "\"", readLine( process.getInputStream() ) );
			for( int index = 0; index < steps; index++ ) {
				check( "SUCCESS " + UpdateTask.PAUSE + " 100 \"update step " + index + "\"", readLine( process.getInputStream() ) );
			}
			steps = 2;
		}

		check( "SUCCESS log \"updates complete\"", readLine( process.getInputStream() ) );
		check( null, readLine( process.getInputStream() ) );
	}

	public final void run() {
		String modulePath = System.getProperty( "jdk.module.path" );
		String mainModule = Program.class.getModule().getName();
		String mainClass = Program.class.getName();

		ProcessBuilder processBuilder = new ProcessBuilder( ProcessCommands.forModule( null, modulePath, mainModule, mainClass ) );
		processBuilder.command().addAll( getProgramCommands() );

		try {
			Process process = processBuilder.start();

			OutputStream output = process.getOutputStream();
			for( String command : getProcessCommands() ) {
				output.write( command.getBytes( TextUtil.CHARSET ) );
			}
			output.close();

			watch( process );
			check( process );
			System.err.println( "Command check=success");
		} catch( Exception exception ) {
			exception.printStackTrace();
		}
	}

	public void watch( Process process ) {
		Thread thread = new Thread( () -> {
			String line;
			try {
				BufferedReader reader = new BufferedReader( new InputStreamReader( process.getErrorStream(), TextUtil.CHARSET ) );
				while( (line = reader.readLine()) != null ) {
					System.err.println( line );
				}
			} catch( IOException exception ) {
				exception.printStackTrace( System.err );
			}
		}, "process-watcher" );
		thread.setDaemon( true );
		thread.start();
	}

	protected String readLine( InputStream input ) throws IOException {
		return new BufferedReader( new InputStreamReader( input, TextUtil.CHARSET ) ).readLine();
	}

	protected void check( String expected, String result ) {
		if( !Objects.equals( expected, result ) ) throw new RuntimeException( "Unexpected result\nexpected: " + expected + "\n     was: " + result );
	}

}
