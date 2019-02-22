package com.xeomar.xevra.task;

import com.xeomar.xevra.TaskResult;
import com.xeomar.xevra.TaskStatus;
import com.xeomar.xevra.UpdateTask;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

public class LaunchTaskTest extends AbstractUpdateTaskTest {

	@Test
	public void testExecute() throws Exception {
		List<TaskResult> results = program.runTasksFromString( UpdateTask.LAUNCH + " java" );
		assertTaskResult( results.get( 0 ), TaskStatus.SUCCESS, "java" );
	}

	@Test
	public void testExecuteFailure() throws Exception {
		List<TaskResult> results = program.runTasksFromString( UpdateTask.LAUNCH + " invalid" );
		assertTaskResult( results.get( 0 ), TaskStatus.FAILURE );
		assertThat( results.get(0).getMessage(), startsWith( "IOException: Cannot run program \"invalid\"" ) );
	}

}
