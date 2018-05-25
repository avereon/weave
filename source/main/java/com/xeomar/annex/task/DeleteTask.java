package com.xeomar.annex.task;

import com.xeomar.annex.AnnexTask;
import com.xeomar.annex.TaskResult;
import com.xeomar.annex.TaskStatus;
import com.xeomar.annex.UpdateTask;
import com.xeomar.util.FileUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class DeleteTask extends AnnexTask {

	private Path target;

	public DeleteTask( List<String> parameters ) {
		super( UpdateTask.DELETE, parameters );
		target = Paths.get( getParameters().get( 0 ) );
	}

	@Override
	public boolean needsElevation() {
		return !Files.isWritable( Paths.get( getParameters().get( 0 ) ) );
	}

	@Override
	public TaskResult execute() throws Exception {
		FileUtil.delete( target );
		return new TaskResult( this, TaskStatus.SUCCESS, target.toString() );
	}

}
