package com.xeomar.annex;

import org.junit.Ignore;
import org.junit.Test;

public class ProgramProgressTest {

	@Test
	@Ignore
	public void testProgressWindow() throws Exception {
		Program program = new Program();
		program.run( new String[] {"--title", "HELLO" } );
		Thread.sleep( 2000 );

	}

}
