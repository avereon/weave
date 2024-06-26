package com.avereon.weave.task;

import com.avereon.weave.TaskResult;
import com.avereon.weave.TaskStatus;
import com.avereon.weave.UpdateTask;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class LaunchTaskTest extends TaskTest {

	private final String workingFolder = System.getProperty( "user.dir" );

	@Test
	public void testLaunch() throws Exception {
		List<TaskResult> results = program.runTasksFromString( UpdateTask.LAUNCH + " " + workingFolder + " java" );
		assertTaskResult( results.get( 0 ), TaskStatus.SUCCESS, "java" );
	}

	@Test
	public void testLaunchFailure() throws Exception {
		List<TaskResult> results = program.runTasksFromString( UpdateTask.LAUNCH + " " + workingFolder + " invalid" );
		assertTaskResult( results.get( 0 ), TaskStatus.FAILURE );
		assertThat( results.get( 0 ).getMessage() ).startsWith( "IOException: Cannot run program \"invalid\"" );
	}

}
