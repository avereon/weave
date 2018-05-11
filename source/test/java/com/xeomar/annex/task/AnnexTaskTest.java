package com.xeomar.annex.task;

import com.xeomar.annex.Program;
import com.xeomar.annex.TaskResult;
import com.xeomar.annex.TaskStatus;
import com.xeomar.util.LogFlag;
import com.xeomar.util.LogUtil;
import com.xeomar.util.Parameters;
import org.junit.Before;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

abstract class AnnexTaskTest {

	protected Program program;

	@Before
	public void setup() {
		this.program = new Program();
		LogUtil.configureLogging( program, Parameters.parse( LogFlag.LOG_LEVEL, "off" ) );
	}

	void assertTaskResult( TaskResult result, TaskStatus status ) {
		assertTaskResult( result, status, null );
	}

	void assertTaskResult( TaskResult result, TaskStatus status, String message ) {
		assertThat(result.getMessage(), result.getStatus(), is( status ) );
		if( message != null ) assertThat( result.getMessage(), is( message ) );
	}

}
