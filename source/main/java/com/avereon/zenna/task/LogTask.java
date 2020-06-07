package com.avereon.zenna.task;

import com.avereon.zenna.Task;
import com.avereon.zenna.TaskResult;
import com.avereon.zenna.TaskStatus;
import com.avereon.zenna.UpdateTask;

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
		return new TaskResult( this, TaskStatus.SUCCESS, "\"" + message + "\"" );
	}

}
