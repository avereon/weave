package com.xeomar.zenna.task;

import com.xeomar.zenna.Task;
import com.xeomar.zenna.TaskResult;
import com.xeomar.zenna.TaskStatus;
import com.xeomar.zenna.UpdateTask;
import com.xeomar.util.TextUtil;

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
