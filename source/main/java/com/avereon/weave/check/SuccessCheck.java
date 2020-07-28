package com.avereon.weave.check;

import com.avereon.weave.UpdateTask;

import java.util.ArrayList;
import java.util.List;

public class SuccessCheck extends CommandCheck {

	public static void main( String[] commands ) {
		new SuccessCheck().run();
	}

	@Override
	protected String getLogFile() {
		return "target/success-check.log";
	}

	@Override
	public List<String> getProcessCommands() {
		List<String> commands = new ArrayList<>();
		commands.add( UpdateTask.LOG + " \"starting updates...\"\n" );

		List<String> groups = new ArrayList<>( parameters.keySet() );
		groups.sort( null );
		for( String groupName : groups ) {
			String duration = parameters.get( groupName )[ 0 ];
			int steps = Integer.parseInt( parameters.get( groupName )[ 1 ] );
			commands.add( UpdateTask.HEADER + " \"Updating Product " + groupName + "\"\n" );
			for( int index = 0; index < steps; index++ ) {
				commands.add( UpdateTask.PAUSE + " " + duration + " \"Step " + (index + 1) + "\"\n" );
			}
		}

		commands.add( UpdateTask.LOG + " \"updates complete\"\n" );
		return commands;
	}

	@Override
	public void check( Process process, boolean verifyLast ) throws Exception {
		check( "SUCCESS log \"starting updates...\"", readLine( process.getInputStream() ) );

		List<String> groups = new ArrayList<>( parameters.keySet() );
		groups.sort( null );
		for( String groupName : groups ) {
			String duration = parameters.get( groupName )[ 0 ];
			int steps = Integer.parseInt( parameters.get( groupName )[ 1 ] );
			check( "SUCCESS " + UpdateTask.HEADER + " \"Updating Product " + groupName + "\"", readLine( process.getInputStream() ) );
			for( int index = 0; index < steps; index++ ) {
				check( "SUCCESS " + UpdateTask.PAUSE + " " + duration + " \"Step " + (index + 1) + "\"", readLine( process.getInputStream() ) );
			}
		}

		check( "SUCCESS log \"updates complete\"", readLine( process.getInputStream() ) );
		check( null, readLine( process.getInputStream() ) );
	}

}
