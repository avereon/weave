package com.avereon.weave.check;

import com.avereon.weave.UpdateFlag;

import java.util.ArrayList;
import java.util.List;

public class SuccessCheckDarkUi extends SuccessCheck {

	public static void main( String[] commands ) {
		new SuccessCheckDarkUi().run();
	}

	@Override
	protected String getLogFile() {
		return "target/success-check-ui.log";
	}

	@Override
	public List<String> getProgramCommands() {
		List<String> commands = new ArrayList<>( super.getProgramCommands() );
		commands.addAll( List.of( UpdateFlag.DARK, UpdateFlag.TITLE, "Success Check" ) );
		return commands;
	}

}
