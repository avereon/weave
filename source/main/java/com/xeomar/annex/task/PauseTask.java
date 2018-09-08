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
	public String getMessage() {
		return getParameters().size() > 1 ? getParameters().get( 1 ) : null;
	}

	@Override
	public TaskResult execute() throws Exception {
		long time = Long.parseLong( getParameters().get( 0 ) );
		Thread.sleep( time );
		return new TaskResult( this, TaskStatus.SUCCESS, "paused " + String.valueOf( time ) + "ms" );
	}

}
