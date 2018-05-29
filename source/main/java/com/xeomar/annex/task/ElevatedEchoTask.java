package com.xeomar.annex.task;

import com.xeomar.annex.AnnexTask;
import com.xeomar.annex.TaskResult;
import com.xeomar.annex.TaskStatus;
import com.xeomar.annex.UpdateTask;
import com.xeomar.util.OperatingSystem;
import com.xeomar.util.TextUtil;

import java.util.List;

public class ElevatedEchoTask extends AnnexTask {

	public ElevatedEchoTask( List<String> parameters ) {
		super( UpdateTask.ELEVATED_ECHO, parameters );
	}

	@Override
	public String getMessage() {
		return getParameters().get( 0 );
	}

	@Override
	public boolean needsElevation() {
		return !OperatingSystem.isProcessElevated();
	}

	@Override
	public TaskResult execute() throws Exception {
		return new TaskResult( this, TaskStatus.SUCCESS, TextUtil.toString( getParameters(), " " ) );
	}

}
