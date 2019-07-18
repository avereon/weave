package com.avereon.zenna.task;

import com.avereon.zenna.Task;
import com.avereon.zenna.TaskResult;
import com.avereon.zenna.TaskStatus;
import com.avereon.zenna.UpdateTask;
import com.avereon.util.TextUtil;

import java.util.List;

public class EchoTask extends Task {

	private String message;

	public EchoTask( List<String> parameters ) {
		super( UpdateTask.ECHO, parameters );
		this.message = getParameters().get( 0 );
	}

	@Override
	public int getStepCount() {
		return 1;
	}

	@Override
	public TaskResult execute() throws Exception {
		setMessage( message );
		incrementProgress();
		return new TaskResult( this, TaskStatus.SUCCESS, TextUtil.toString( getParameters(), " " ) );
	}

}
