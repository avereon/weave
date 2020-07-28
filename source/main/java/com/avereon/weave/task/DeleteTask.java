package com.avereon.weave.task;

import com.avereon.util.FileUtil;
import com.avereon.util.Log;
import com.avereon.weave.Task;
import com.avereon.weave.TaskResult;
import com.avereon.weave.TaskStatus;
import com.avereon.weave.UpdateTask;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class DeleteTask extends Task {

	private static final System.Logger log = Log.get();

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
		if( !Files.exists( target ) ) log.log( Log.WARN, "Target does not exist: {0}", target );
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
