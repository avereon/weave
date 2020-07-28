package com.avereon.weave.task;

import com.avereon.weave.Task;
import com.avereon.weave.TaskResult;
import com.avereon.weave.TaskStatus;
import com.avereon.weave.UpdateTask;

import java.util.List;

public class HeaderTask extends Task {

	private final String header;

	public HeaderTask( List<String> parameters ) {
		super( UpdateTask.HEADER, parameters );
		this.header = getParameters().get( 0 );
	}

	@Override
	public int getStepCount() {
		return 0;
	}

	@Override
	public TaskResult execute() throws Exception {
		setHeader( header );
		return new TaskResult( this, TaskStatus.SUCCESS, header == null ? "" : "\"" + header + "\"" );
	}

	@Override
	public TaskResult rollback() throws Exception {
		decrementProgress();
		return new TaskResult( this, TaskStatus.SUCCESS, header == null ? "Rollback" : "\"Rollback " + header + "\"" );
	}

}
