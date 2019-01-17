package com.xeomar.xevra;

import com.xeomar.util.LogFlag;

public class ProgressDialogCheck {

	public static void main( String[] commands ) {
		StringBuilder builder = new StringBuilder();
		for( int index = 0; index < 10; index++ ) {
			builder.append( "pause 200 \"Task " ).append( index ).append( "\"" ).append( "\n" );
		}

		try {
			Program program = new Program();
			program.start( new String[]{ UpdateFlag.TITLE, "Program Update Check", LogFlag.LOG_LEVEL, "debug", InternalFlag.STRING } );
			program.waitForStart();
			new Thread( () -> {
				try {
					program.runTasksFromString( builder.toString() );
				} catch( Exception exception ) {
					exception.printStackTrace( System.err );
				}
			} ).start();
		} catch( Throwable throwable ) {
			throwable.printStackTrace( System.err );
		}
	}

}
