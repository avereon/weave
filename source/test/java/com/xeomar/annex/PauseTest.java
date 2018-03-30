package com.xeomar.annex;

import org.junit.Test;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class PauseTest {

	@Test
	public void testExecute() throws Exception {
		long delay = 50L;
		long start = System.currentTimeMillis();
		TaskResult result = TaskResult.parse( new Program().runTasksFromString( UpdateFlag.PAUSE + " " + delay ) );
		long stop = System.currentTimeMillis();

		assertThat( stop - start, is( greaterThanOrEqualTo( delay ) ) );
		assertThat( result.getStatus(), is( TaskStatus.SUCCESS ) );
		assertThat( result.getMessage(), is( "success" ) );
	}

	@Test
	public void testExecuteFailure() throws Exception {
		TaskResult result = TaskResult.parse( new Program().runTasksFromString( UpdateFlag.PAUSE + " forever" ) );

		assertThat( result.getStatus(), is( TaskStatus.FAILURE ) );
		assertThat( result.getMessage(), is( "NumberFormatException: For input string: \"forever\"" ) );
	}

}
