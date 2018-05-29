package com.xeomar.annex.task;

import com.xeomar.annex.AnnexTask;
import com.xeomar.annex.TaskResult;
import com.xeomar.annex.TaskStatus;
import com.xeomar.annex.UpdateTask;
import com.xeomar.util.TextUtil;

import java.util.List;

public class EchoTask extends AnnexTask {

	public EchoTask( List<String> parameters ) {
		super( UpdateTask.ECHO, parameters );
	}

	@Override
	public String getMessage() {
		return getParameters().get( 0 );
	}

	@Override
	public TaskResult execute() throws Exception {
		return new TaskResult( this, TaskStatus.SUCCESS, TextUtil.toString( getParameters(), " " ) );
	}

}
