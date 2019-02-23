package com.xeomar.xevra.task;

import com.xeomar.xevra.AbstractUpdateTask;
import com.xeomar.xevra.TaskResult;
import com.xeomar.xevra.TaskStatus;
import com.xeomar.xevra.UpdateTask;

import java.util.List;

public class PauseTask extends AbstractUpdateTask {

	// The number of milliseconds per increment
	private static long increment = 20;

	private String message;

	private long time;

	private int steps;

	public PauseTask( List<String> parameters ) {
		super( UpdateTask.PAUSE, parameters );
		this.message = getParameters().size() > 1 ? getParameters().get( 1 ) : null;
	}

	@Override
	public int getStepCount() {
		this.time = Long.parseLong( getParameters().get( 0 ) );
		this.steps = (int)(time / increment) + 1;
		return steps;
	}

	@Override
	public TaskResult execute() throws Exception {
		setMessage( message );
		for( int index = 0; index < steps; index++ ) {
			Thread.sleep( increment );
			incrementProgress();
		}
		return new TaskResult( this, TaskStatus.SUCCESS, "paused " + time + "ms" );
	}

}
