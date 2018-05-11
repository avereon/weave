package com.xeomar.annex.task;

import com.xeomar.annex.TaskResult;
import com.xeomar.annex.TaskStatus;
import com.xeomar.annex.UpdateTask;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

public class LaunchTaskTest extends AnnexTaskTest {

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
