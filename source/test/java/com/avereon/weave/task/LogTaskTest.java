package com.avereon.weave.task;

import com.avereon.weave.TaskResult;
import com.avereon.weave.TaskStatus;
import com.avereon.weave.UpdateTask;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class LogTaskTest extends TaskTest {

	@Test
	public void testExecute() throws Exception {
		List<TaskResult> results = program.runTasksFromString( UpdateTask.LOG + " \"HELLO WORLD\"" );
		assertTaskResult( results.get( 0 ), TaskStatus.SUCCESS );

		assertThat( results.get( 0 ).getTask().getParameters().get( 0 ) ).isEqualTo( "HELLO WORLD" );
	}

}
