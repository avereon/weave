package com.avereon.weave.check;

import com.avereon.weave.UpdateTask;

import java.util.ArrayList;
import java.util.List;

public class FailureCheckUiWithRelaunch extends FailureCheckUi {

	public static void main( String[] commands ) {
		new FailureCheckUiWithRelaunch().run();
	}

	@Override
	protected String getLogFile() {
		return "target/failure-check-ui-relaunch.log";
	}

	@Override
	public List<String> getProcessCommands() {
		List<String> superCommands = super.getProcessCommands();

		ArrayList<String> commands = new ArrayList<>( superCommands.subList( 0, superCommands.size() - 1 ) );
		commands.add( UpdateTask.LAUNCH + " " + System.getProperty( "user.dir" ) + " echo" + "\n" );
		commands.add( UpdateTask.LOG + " \"updates complete\"\n" );

		return commands;
	}

	@Override
	public void check( Process process, boolean verifyLast ) throws Exception {
		super.check( process, false );
		check( "SUCCESS launch echo", readLine( process.getInputStream() ) );
		if( verifyLast ) check( null, readLine( process.getInputStream() ) );
		if( verifyLast ) System.err.println( "NOTE - Progress dialog should NOT be open" );
	}

}
