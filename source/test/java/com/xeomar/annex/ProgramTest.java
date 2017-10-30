package com.xeomar.annex;

import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ProgramTest {

	@Test
	public void testReadCommandsFromBytes() throws Exception {
		List<AnnexTask> tasks = Program.INSTANCE.readTasksFromBytes( "update source target\nupdate source2 target2".getBytes( "utf-8" ) );

		assertThat( tasks.get( 0 ).getParameters().get( 0 ), is( "source" ) );
		assertThat( tasks.get( 0 ).getParameters().get( 1 ), is( "target" ) );
		assertThat( tasks.get( 1 ).getParameters().get( 0 ), is( "source2" ) );
		assertThat( tasks.get( 1 ).getParameters().get( 1 ), is( "target2" ) );
		assertThat( tasks.get( 0 ).getParameters().size(), is( 2 ) );
		assertThat( tasks.get( 1 ).getParameters().size(), is( 2 ) );
		assertThat( tasks.size(), is( 2 ) );
	}

}
