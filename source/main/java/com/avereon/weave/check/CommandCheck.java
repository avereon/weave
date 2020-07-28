package com.avereon.weave.check;

import com.avereon.util.LogFlag;
import com.avereon.util.ProcessCommands;
import com.avereon.util.TextUtil;
import com.avereon.weave.Launcher;
import com.avereon.weave.UpdateFlag;

import java.io.*;
import java.util.*;

public abstract class CommandCheck {

	private static final int PRODUCT_COUNT = 7;

	private static final Random random = new Random();

	public Map<String, String[]> parameters;

	CommandCheck() {
		parameters = new HashMap<>();
		for( int groupIndex = 0; groupIndex < PRODUCT_COUNT; groupIndex++ ) {
			int steps = random.nextInt( 3 ) + 2;
			parameters.put( String.valueOf( (char)(groupIndex + 65) ), new String[]{ "100", String.valueOf( steps ) } );
		}
	}

	public List<String> getProgramCommands() {
		List<String> commands = new ArrayList<>();
		commands.add( UpdateFlag.STDIN );
		commands.add( LogFlag.LOG_FILE );
		commands.add( getLogFile() );
		//commands.add( LogFlag.LOG_LEVEL );
		//commands.add( LogFlag.DEBUG );
		return commands;
	}

	protected String getLogFile() {
		return "target/check.log";
	}

	public List<String> getProcessCommands() {
		return List.of();
	}

	public void check( Process process, boolean verifyLast ) throws Exception {}

	public final void run() {
		ProcessBuilder processBuilder = new ProcessBuilder( ProcessCommands.forLauncher( Launcher.class ) );
		processBuilder.command().addAll( getProgramCommands() );

		try {
			Process process = processBuilder.start();

			OutputStream output = process.getOutputStream();
			for( String command : getProcessCommands() ) {
				output.write( command.getBytes( TextUtil.CHARSET ) );
			}
			output.close();

			watch( process );
			check( process, true );
			System.err.println( "Command check=success" );
		} catch( Exception exception ) {
			exception.printStackTrace();
			System.exit( -1 );
		}
	}

	public void watch( Process process ) {
		Thread thread = new Thread( () -> {
			String line;
			try {
				BufferedReader reader = new BufferedReader( new InputStreamReader( process.getErrorStream(), TextUtil.CHARSET ) );
				while( (line = reader.readLine()) != null ) {
					System.err.println( "> " + line );
				}
				System.err.println("> eof");
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
