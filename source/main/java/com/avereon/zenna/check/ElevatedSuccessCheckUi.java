package com.avereon.zenna.check;

import com.avereon.zenna.UpdateFlag;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to test elevated command execution.
 * <p>
 * It is not a unit test because it causes an elevated privileges dialog to be
 * displayed, requiring user input. It is also not in the test folder because
 * IntelliJ keys off the folder whether to run this as a module or a class. It
 * needs to be run from the java folder, not the test folder.
 */
public class ElevatedSuccessCheckUi extends ElevatedSuccessCheck {

	public static void main( String[] commands ) {
		new ElevatedSuccessCheckUi().run();
	}

	@Override
	protected String getLogFile() {
		return "target/elevated-success-check-ui.log";
	}

	@Override
	public List<String> getProgramCommands() {
		List<String> commands = new ArrayList<>( super.getProgramCommands() );
		commands.addAll( List.of( UpdateFlag.TITLE, "Elevated Success Check" ) );
		return commands;
	}

}
