package com.xeomar.xevra.task;

import com.xeomar.xevra.AnnexTask;
import com.xeomar.xevra.TaskResult;
import com.xeomar.xevra.TaskStatus;
import com.xeomar.xevra.UpdateTask;
import com.xeomar.util.TextUtil;

import java.util.List;

public class EchoTask extends AnnexTask {

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
