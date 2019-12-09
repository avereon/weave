package com.avereon.zenna.task;

import com.avereon.zenna.Task;
import com.avereon.zenna.TaskResult;
import com.avereon.zenna.TaskStatus;
import com.avereon.zenna.UpdateTask;
import com.avereon.util.FileUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class DeleteTask extends Task {

	private Path target;

	public DeleteTask( List<String> parameters ) {
		super( UpdateTask.DELETE, parameters );
		this.target = Paths.get( getParameters().get( 0 ) );
	}

	@Override
	public int getStepCount() {
		return 1;
	}

	@Override
	public boolean needsElevation() {
		return Files.exists( target ) && !Files.isWritable( target );
	}

	@Override
	public TaskResult execute() throws Exception {
		setMessage( "Delete " + target );
		FileUtil.delete( target );
		incrementProgress();
		return new TaskResult( this, TaskStatus.SUCCESS, target.toString() );
	}

}
