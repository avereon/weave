package com.avereon.zenna.check;

import com.avereon.zenna.UpdateTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FailureCheck extends CommandCheck {

	private final String failureGroup = "F";

	public static void main( String[] commands ) {
		new FailureCheck().run();
	}

	@Override
	protected String getLogFile() {
		return "target/failure-check.log";
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
			if( failureGroup.equals( groupName ) ) {
				commands.add( UpdateTask.FAIL + " \"Failed updating product " + groupName + "\"" );
			} else {
				for( int index = 0; index < steps; index++ ) {
					commands.add( UpdateTask.PAUSE + " " + duration + " \"Step " + (index + 1) + "\"\n" );
				}
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
			if( failureGroup.equals( groupName ) ) {
				check( "FAILURE " + UpdateTask.FAIL + " \"Failed updating product " + groupName + "\"", readLine( process.getInputStream() ) );
				check( "SUCCESS " + UpdateTask.HEADER + " \"Rollback Updating Product " + groupName + "\"", readLine( process.getInputStream() ) );
				break;
			} else {
				for( int index = 0; index < steps; index++ ) {
					check( "SUCCESS " + UpdateTask.PAUSE + " " + duration + " \"Step " + (index + 1) + "\"", readLine( process.getInputStream() ) );
				}
			}
		}

		groups = groups.subList( 0, groups.size() - 2 );
		Collections.reverse( groups );
		for( String groupName : groups ) {
			int steps = Integer.parseInt( parameters.get( groupName )[ 1 ] ) - 1;
			for( int index = steps; index >= 0; index-- ) {
				check( "SUCCESS " + UpdateTask.PAUSE + " \"Rollback Step " + (index + 1) + "\"", readLine( process.getInputStream() ) );
			}
			check( "SUCCESS " + UpdateTask.HEADER + " \"Rollback Updating Product " + groupName + "\"", readLine( process.getInputStream() ) );
		}
		check( "SUCCESS log \"Rollback starting updates...\"", readLine( process.getInputStream() ) );
		if( verifyLast ) check( null, readLine( process.getInputStream() ) );
	}

}
