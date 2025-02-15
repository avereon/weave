package com.avereon.weave.check;

import com.avereon.weave.WeaveFlag;

import java.util.ArrayList;
import java.util.List;

public class FailureCheckUi extends FailureCheck {

	public static void main( String[] commands ) {
		new FailureCheckUi().run();
	}

	@Override
	protected String getLogFile() {
		return "target/failure-check-ui.log";
	}

	@Override
	public List<String> getProgramCommands() {
		List<String> commands = new ArrayList<>( super.getProgramCommands() );
		commands.addAll( List.of( WeaveFlag.TITLE, "Failure Check" ) );
		return commands;
	}

	@Override
	public void check( Process process, boolean verifyLast ) throws Exception {
		super.check( process, verifyLast );
		if( verifyLast ) System.err.println( "NOTE - Progress dialog should still be open" );
	}

}
