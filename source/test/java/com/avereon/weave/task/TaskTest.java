package com.avereon.weave.task;

import com.avereon.log.Log;
import com.avereon.util.LogFlag;
import com.avereon.util.Parameters;
import com.avereon.weave.Weave;
import com.avereon.weave.TaskResult;
import com.avereon.weave.TaskStatus;
import org.junit.jupiter.api.BeforeEach;

import static org.assertj.core.api.Assertions.assertThat;

abstract class TaskTest {

	protected Weave program;

	@BeforeEach
	public void setup() {
		this.program = new Weave();
		Log.configureLogging( program, Parameters.parse( LogFlag.LOG_LEVEL, "none" ) );
	}

	void assertTaskResult( TaskResult result, TaskStatus status ) {
		assertTaskResult( result, status, null );
	}

	void assertTaskResult( TaskResult result, TaskStatus status, String message ) {
		assertThat( result.getStatus() ).withFailMessage( result.getMessage() ).isEqualTo( status );
		if( message != null ) assertThat( result.getMessage() ).isEqualTo( message );
	}

}
