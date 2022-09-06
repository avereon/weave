package com.avereon.weave.task;

import com.avereon.weave.Task;
import com.avereon.weave.TaskResult;
import com.avereon.weave.TaskStatus;
import com.avereon.weave.UpdateTask;

import java.util.List;

public class LogTask extends Task {

	private final String message;

	public LogTask( List<String> parameters ) {
		super( UpdateTask.LOG, parameters );
		this.message = getParameters().get( 0 );
	}

	@Override
	public int getStepCount() {
		return 1;
	}

	@Override
	public TaskResult execute() throws Exception {
		incrementProgress();
		return new TaskResult( this, TaskStatus.SUCCESS, message == null ? "" : "\"" + message + "\"" );
	}

	@Override
	public TaskResult rollback() throws Exception {
		decrementProgress();
		return new TaskResult( this, TaskStatus.ROLLBACK, message == null ? "" : "\"" + message + "\"" );
	}

}
