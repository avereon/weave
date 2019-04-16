package com.xeomar.zenna.task;

import com.xeomar.zenna.Task;
import com.xeomar.zenna.TaskResult;
import com.xeomar.zenna.TaskStatus;
import com.xeomar.zenna.UpdateTask;
import com.xeomar.util.FileUtil;

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
		return !Files.isWritable( Paths.get( getParameters().get( 0 ) ) );
	}

	@Override
	public TaskResult execute() throws Exception {
		setMessage( "Delete " + target );
		FileUtil.delete( target );
		incrementProgress();
		return new TaskResult( this, TaskStatus.SUCCESS, target.toString() );
	}

}
