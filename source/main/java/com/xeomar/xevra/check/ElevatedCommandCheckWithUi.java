package com.xeomar.xevra.check;

import com.xeomar.xevra.UpdateFlag;

import java.util.List;

/**
 * This class is used to test elevated command execution.
 * <p>
 * It is not a unit test because it causes an elevated privileges dialog to be
 * displayed, requiring user input. It is also not in the test folder because
 * IntelliJ keys off the folder whether to run this as a module or a class. It
 * needs to be run from the java folder, not the test folder.
 */
public class ElevatedCommandCheckWithUi extends ElevatedCommandCheck {

	public static void main( String[] commands ) {
		new ElevatedCommandCheckWithUi().run();
	}

	@Override
	public List<String> getAdditionalCommands() {
		return List.of( UpdateFlag.TITLE, "ElevatedCommandCheck" );
	}

}
