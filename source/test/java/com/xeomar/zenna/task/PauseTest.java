package com.xeomar.zenna.task;

import com.xeomar.zenna.TaskResult;
import com.xeomar.zenna.TaskStatus;
import com.xeomar.zenna.UpdateTask;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class PauseTest extends TaskTest {

	@Test
	public void testExecute() throws Exception {
		long delay = 50L;
		long start = System.currentTimeMillis();
		List<TaskResult> results = program.runTasksFromString( UpdateTask.PAUSE + " " + delay ) ;
		long stop = System.currentTimeMillis();

		assertThat( stop - start, is( greaterThanOrEqualTo( delay ) ) );
		assertTaskResult( results.get(0), TaskStatus.SUCCESS );
	}

	@Test
	public void testExecuteFailure() throws Exception {
		List<TaskResult> results =  program.runTasksFromString( UpdateTask.PAUSE + " forever" ) ;
		assertTaskResult( results.get(0), TaskStatus.FAILURE, "NumberFormatException: For input string: \"forever\"" );
	}

}
