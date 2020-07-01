package com.avereon.zenna.check;

import com.avereon.zenna.UpdateTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ElevatedSuccessCheck extends SuccessCheck {

	public static void main( String[] commands ) {
		new ElevatedSuccessCheck().run();
	}

	@Override
	protected String getLogFile() {
		return "target/elevated-success-check.log";
	}

	@Override
	public List<String> getProcessCommands() {
		List<String> commands = new ArrayList<>();
		commands.add( UpdateTask.PAUSE + " 500 \"Preparing update\"\n" );
		commands.add( UpdateTask.ELEVATED_LOG + " hello1\n" );
		commands.add( UpdateTask.ELEVATED_PAUSE + " 2000 \"Simulating update\"\n" );
		commands.add( UpdateTask.ELEVATED_LOG + " hello2\n" );
		commands.add( UpdateTask.PAUSE + " 500 \"Finishing update\"\n" );
		return commands;
	}

	@Override
	public void check( Process process, boolean verifyLast ) throws IOException {
		check( "SUCCESS pause 500 \"Preparing update\"", readLine( process.getInputStream() ) );
		check( "SUCCESS log \"hello1\"", readLine( process.getInputStream() ) );
		check( "SUCCESS pause 2000 \"Simulating update\"", readLine( process.getInputStream() ) );
		check( "SUCCESS log \"hello2\"", readLine( process.getInputStream() ) );
		check( "SUCCESS pause 500 \"Finishing update\"", readLine( process.getInputStream() ) );
	}

}
