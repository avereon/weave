package com.avereon.zenna.task;

import com.avereon.zenna.TaskResult;
import com.avereon.zenna.TaskStatus;
import com.avereon.zenna.UpdateTask;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;

public class LaunchTaskTest extends TaskTest {

	private String workingFolder = System.getProperty( "user.dir" );

	@Test
	public void testExecute() throws Exception {
		List<TaskResult> results = program.runTasksFromString( UpdateTask.LAUNCH + " " + workingFolder + " java" );
		assertTaskResult( results.get( 0 ), TaskStatus.SUCCESS, "java" );
	}

	@Test
	public void testExecuteFailure() throws Exception {
		List<TaskResult> results = program.runTasksFromString( UpdateTask.LAUNCH + " " + workingFolder + " invalid" );
		assertTaskResult( results.get( 0 ), TaskStatus.FAILURE );
		assertThat( results.get(0).getMessage(), startsWith( "IOException: Cannot run program \"invalid\"" ) );
	}

}
