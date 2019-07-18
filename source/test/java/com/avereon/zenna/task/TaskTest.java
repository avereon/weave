package com.avereon.zenna.task;

import com.avereon.zenna.Program;
import com.avereon.zenna.TaskResult;
import com.avereon.zenna.TaskStatus;
import com.avereon.util.LogFlag;
import com.avereon.util.LogUtil;
import com.avereon.util.Parameters;
import org.junit.Before;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

abstract class TaskTest {

	protected Program program;

	@Before
	public void setup() {
		this.program = new Program();
		LogUtil.configureLogging( program, Parameters.parse( LogFlag.LOG_LEVEL, "none" ) );
	}

	void assertTaskResult( TaskResult result, TaskStatus status ) {
		assertTaskResult( result, status, null );
	}

	void assertTaskResult( TaskResult result, TaskStatus status, String message ) {
		assertThat(result.getMessage(), result.getStatus(), is( status ) );
		if( message != null ) assertThat( result.getMessage(), is( message ) );
	}

}
