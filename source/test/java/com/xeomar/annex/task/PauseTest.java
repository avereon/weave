package com.xeomar.annex.task;

import com.xeomar.annex.Program;
import com.xeomar.annex.TaskResult;
import com.xeomar.annex.TaskStatus;
import com.xeomar.annex.UpdateTask;
import org.hamcrest.Matchers;
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
		List<TaskResult> results = new Program().runTasksFromString( UpdateTask.PAUSE + " " + delay ) ;
		long stop = System.currentTimeMillis();

		assertThat( stop - start, is( greaterThanOrEqualTo( delay ) ) );
		assertTaskResult( results.get(0), TaskStatus.SUCCESS, "success" );
	}

	@Test
	public void testExecuteFailure() throws Exception {
		List<TaskResult> results =  new Program().runTasksFromString( UpdateTask.PAUSE + " forever" ) ;
		assertTaskResult( results.get(0), TaskStatus.FAILURE, "NumberFormatException: For input string: \"forever\"" );
	}

}
