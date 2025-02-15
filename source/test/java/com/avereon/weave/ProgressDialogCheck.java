package com.avereon.weave;

import com.avereon.util.LogFlag;

import java.util.concurrent.TimeUnit;

public class ProgressDialogCheck {

	public static void main( String[] commands ) {
		StringBuilder builder = new StringBuilder();
		for( int index = 0; index < 10; index++ ) {
			builder.append( "pause 200 \"Task " ).append( index ).append( "\"" ).append( "\n" );
		}

		try {
			Weave program = new Weave();
			program.start( WeaveFlag.TITLE, "Program Update Check", LogFlag.LOG_LEVEL, "debug", InternalFlag.STRING );
			program.waitForStart( 2, TimeUnit.SECONDS );
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
