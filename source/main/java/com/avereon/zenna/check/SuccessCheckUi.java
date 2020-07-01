package com.avereon.zenna.check;

import com.avereon.zenna.UpdateFlag;

import java.util.ArrayList;
import java.util.List;

public class SuccessCheckUi extends SuccessCheck {

	public static void main( String[] commands ) {
		new SuccessCheckUi().run();
	}

	@Override
	protected String getLogFile() {
		return "target/success-check-ui.log";
	}

	@Override
	public List<String> getProgramCommands() {
		List<String> commands = new ArrayList<>( super.getProgramCommands() );
		commands.addAll( List.of( UpdateFlag.TITLE, "Success Check" ) );
		return commands;
	}

}
