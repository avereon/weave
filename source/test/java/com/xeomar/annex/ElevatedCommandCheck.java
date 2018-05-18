package com.xeomar.annex;

public class ElevatedCommandCheck {

	public static void main( String[] commands ) {
		Program program = new Program();
		try {
			program.runTasksFromString( UpdateTask.ELEVATED_ECHO + " hello" );
		} catch( Exception exception ) {
			exception.printStackTrace(System.err);
		}
	}

}
