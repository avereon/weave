package com.avereon.zenna.task;

import com.avereon.zenna.TaskResult;
import com.avereon.zenna.TaskStatus;
import com.avereon.zenna.UpdateTask;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class LogTaskTest extends TaskTest {

	@Test
	public void testExecute() throws Exception {
		List<TaskResult> results = program.runTasksFromString( UpdateTask.LOG + " \"HELLO WORLD\"" );
		assertTaskResult( results.get( 0 ), TaskStatus.SUCCESS );

		assertThat( results.get( 0 ).getTask().getParameters().get( 0 ), is( "HELLO WORLD" ) );
	}

}
