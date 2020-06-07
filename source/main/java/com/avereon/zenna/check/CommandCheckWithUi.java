package com.avereon.zenna.check;

import com.avereon.zenna.UpdateFlag;

import java.util.ArrayList;
import java.util.List;

public class CommandCheckWithUi extends CommandCheck {

	public static void main( String[] commands ) {
		new CommandCheckWithUi().run();
	}

	@Override
	protected String getLogFile() {
		return "target/check-ui.log";
	}

	@Override
	public List<String> getProgramCommands() {
		List<String> commands = new ArrayList<>( super.getProgramCommands() );
		commands.addAll( List.of( UpdateFlag.TITLE, "CommandCheck" ) );
		return commands;
	}

}
