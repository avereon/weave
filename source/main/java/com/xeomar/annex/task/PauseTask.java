package com.xeomar.annex.task;

import com.xeomar.annex.AnnexTask;
import com.xeomar.annex.TaskResult;
import com.xeomar.annex.TaskStatus;
import com.xeomar.annex.UpdateTask;

import java.util.List;

public class PauseTask extends AnnexTask {

	private String message;

	public PauseTask( List<String> parameters ) {
		super( UpdateTask.PAUSE, parameters );
		this.message = getParameters().size() > 1 ? getParameters().get( 1 ) : null;
	}

	@Override
	public int getStepCount() {
		return 1;
	}

	@Override
	public TaskResult execute() throws Exception {
		setMessage( message );
		long time = Long.parseLong( getParameters().get( 0 ) );
		Thread.sleep( time );
		incrementProgress();
		return new TaskResult( this, TaskStatus.SUCCESS, "paused " + String.valueOf( time ) + "ms" );
	}

}
