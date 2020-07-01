package com.avereon.zenna.task;

import com.avereon.util.Log;
import com.avereon.zenna.Task;
import com.avereon.zenna.TaskResult;
import com.avereon.zenna.TaskStatus;
import com.avereon.zenna.UpdateTask;

import java.lang.System.Logger;
import java.util.List;

public class PauseTask extends Task {

	@SuppressWarnings( "unused" )
	private static final Logger log = Log.get();

	// The number of milliseconds per increment
	private static final long increment = 10;

	private final String message;

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
	@SuppressWarnings( "BusyWait" )
	public TaskResult execute() throws Exception {
		setMessage( message );
		for( int index = 0; index < steps; index++ ) {
			Thread.sleep( increment );
			incrementProgress();
		}

		return new TaskResult( this, TaskStatus.SUCCESS, time + (message == null ? "" : " \"" + message + "\"") );
	}

	@Override
	public TaskResult rollback() throws Exception {
		decrementProgress();
		return new TaskResult( this, TaskStatus.SUCCESS, message == null ? "Rollback" : "\"Rollback " + message + "\"" );
	}

}
