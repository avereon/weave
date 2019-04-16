package com.xeomar.zenna.task;

import com.xeomar.zenna.TaskResult;
import com.xeomar.zenna.TaskStatus;
import com.xeomar.zenna.UpdateTask;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

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
