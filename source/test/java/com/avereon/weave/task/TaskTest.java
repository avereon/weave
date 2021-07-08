package com.avereon.weave.task;

import com.avereon.util.LogFlag;
import com.avereon.log.Log;
import com.avereon.util.Parameters;
import com.avereon.weave.Program;
import com.avereon.weave.TaskResult;
import com.avereon.weave.TaskStatus;
import org.junit.jupiter.api.BeforeEach;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

abstract class TaskTest {

	protected Program program;

	@BeforeEach
	public void setup() {
		this.program = new Program();
		Log.configureLogging( program, Parameters.parse( LogFlag.LOG_LEVEL, "none" ) );
	}

	void assertTaskResult( TaskResult result, TaskStatus status ) {
		assertTaskResult( result, status, null );
	}

	void assertTaskResult( TaskResult result, TaskStatus status, String message ) {
		assertThat( result.getMessage(), result.getStatus(), is( status ) );
		if( message != null ) assertThat( result.getMessage(), is( message ) );
	}

}
