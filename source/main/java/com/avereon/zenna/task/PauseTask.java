package com.avereon.zenna.task;

import com.avereon.util.LogUtil;
import com.avereon.zenna.Task;
import com.avereon.zenna.TaskResult;
import com.avereon.zenna.TaskStatus;
import com.avereon.zenna.UpdateTask;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.List;

public class PauseTask extends Task {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	// The number of milliseconds per increment
	private static long increment = 100;

	private String message;

	private long time;

	private int steps;

	public PauseTask( List<String> parameters ) {
		super( UpdateTask.PAUSE, parameters );
		this.message = getParameters().size() > 1 ? getParameters().get( 1 ) : null;
	}

	@Override
	public int getStepCount() {
		this.time = Math.max( 100, Long.parseLong( getParameters().get( 0 ) ) );
		this.steps = (int)(time / increment);
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
