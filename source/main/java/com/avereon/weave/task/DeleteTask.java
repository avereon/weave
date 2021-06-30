package com.avereon.weave.task;

import com.avereon.util.FileUtil;
import com.avereon.util.Log;
import com.avereon.weave.Task;
import com.avereon.weave.TaskResult;
import com.avereon.weave.TaskStatus;
import com.avereon.weave.UpdateTask;
import lombok.extern.flogger.Flogger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Flogger
public class DeleteTask extends Task {

	private final Path target;

	public DeleteTask( List<String> parameters ) {
		super( UpdateTask.DELETE, parameters );
		this.target = Paths.get( getParameters().get( 0 ) );
	}

	@Override
	public int getStepCount() {
		return 1;
	}

	@Override
	public void validate() {
		if( !Files.exists( target ) ) log.atFine().log( "Target does not exist: %s", target );
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
