package com.xeomar.annex;

public class ProgressDialogCheck {

	public static void main( String[] commands ) {
		StringBuilder builder = new StringBuilder();
		for( int index = 0; index < 10; index++ ) {
			builder.append( "pause 100 \"Task " ).append( index ).append( "\"" ).append( "\n" );
		}

		try {
			Program program = new Program();
			program.start( new String[]{ UpdateFlag.TITLE, "Program Update Check", InternalFlag.STRING } );
			new Thread( () -> {
				try {
					new Program().runTasksFromString( builder.toString() );
				} catch( Exception exception ) {
					exception.printStackTrace( System.err );
				}
			} ).start();
		} catch( Throwable throwable ) {
			throwable.printStackTrace( System.err );
		}
	}

}
