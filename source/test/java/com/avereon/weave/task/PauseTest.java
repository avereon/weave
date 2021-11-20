package com.avereon.weave.task;

import com.avereon.weave.TaskResult;
import com.avereon.weave.TaskStatus;
import com.avereon.weave.UpdateTask;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PauseTest extends TaskTest {

	@Test
	public void testExecute() throws Exception {
		long delay = 50L;
		long start = System.currentTimeMillis();
		List<TaskResult> results = program.runTasksFromString( UpdateTask.PAUSE + " " + delay );
		long stop = System.currentTimeMillis();

		assertThat( stop - start ).isGreaterThanOrEqualTo( delay );
		assertTaskResult( results.get( 0 ), TaskStatus.SUCCESS );
	}

	@Test
	public void testExecuteFailure() throws Exception {
		List<TaskResult> results = program.runTasksFromString( UpdateTask.PAUSE + " forever" );
		assertTaskResult( results.get( 0 ), TaskStatus.FAILURE, "NumberFormatException: For input string: \"forever\"" );
	}

}
