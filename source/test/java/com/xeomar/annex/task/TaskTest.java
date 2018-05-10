package com.xeomar.annex.task;

import com.xeomar.annex.TaskResult;
import com.xeomar.annex.TaskStatus;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public abstract class TaskTest {

	protected void assertTaskResult( TaskResult result, TaskStatus status ) {
		assertTaskResult( result, status, null );
	}

	protected void assertTaskResult( TaskResult result, TaskStatus status, String message ) {
		assertThat(result.getMessage(), result.getStatus(), is( status ) );
		if( message != null ) assertThat( result.getMessage(), is( message ) );
	}

}
