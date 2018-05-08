package com.xeomar.annex;

import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class LaunchTaskTest {

	@Test
	public void testExecute() throws Exception {
		TaskResult result = TaskResult.parse( new Program().runTasksFromString( UpdateTask.LAUNCH + " java" ) );
		assertThat( result.getStatus(), is( TaskStatus.SUCCESS ) );
		assertThat( result.getMessage(), is( "java" ) );
	}

	@Test
	public void testExecuteFailure() throws Exception {
		TaskResult result = TaskResult.parse( new Program().runTasksFromString( UpdateTask.LAUNCH + " invalid" ) );
		assertThat( result.getStatus(), is( TaskStatus.FAILURE ) );
		assertThat( result.getMessage(), startsWith( "IOException: Cannot run program \"invalid\"" ) );
	}

}
