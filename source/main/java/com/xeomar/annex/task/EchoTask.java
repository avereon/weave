package com.xeomar.annex.task;

import com.xeomar.annex.AnnexTask;
import com.xeomar.annex.TaskResult;
import com.xeomar.annex.TaskStatus;
import com.xeomar.annex.UpdateTask;
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
