package com.avereon.zenna.task;

import com.avereon.zenna.Task;
import com.avereon.zenna.TaskResult;
import com.avereon.zenna.TaskStatus;
import com.avereon.zenna.UpdateTask;

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
		return new TaskResult( this, TaskStatus.SUCCESS, "\"" + header + "\"" );
	}

}
