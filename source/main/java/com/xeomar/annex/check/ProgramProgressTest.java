package com.xeomar.annex.check;

import com.xeomar.annex.Program;

public class ProgramProgressTest {

	public static void main( String[] commands ) {
		StringBuilder builder = new StringBuilder();
		for( int index = 0; index < 10; index++ ) {
			builder.append( "pause 500 \"Task " ).append( index ).append( "\"" ).append( "\n" );
		}

		try {
			Program program = new Program();
			program.run( new String[]{ "--title", "Program Update Check" } );
			program.runTasksFromString( builder.toString() );
			Runtime.getRuntime().exit( 0 );
		} catch( Throwable throwable ) {
			throwable.printStackTrace( System.err );
		}
	}

}
