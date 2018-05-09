package com.xeomar.annex.task;

import com.xeomar.annex.AnnexTask;
import com.xeomar.annex.TaskResult;
import com.xeomar.annex.TaskStatus;
import com.xeomar.annex.UpdateTask;

import java.util.List;

public class PauseTask extends AnnexTask {

	public PauseTask( List<String> parameters ) {
		super( UpdateTask.PAUSE, parameters );
	}

	@Override
	public TaskResult execute() throws Exception {
		Thread.sleep( Long.parseLong( getParameters().get( 0 ) ) );
		return new TaskResult( this, TaskStatus.SUCCESS, "success" );
	}

}
