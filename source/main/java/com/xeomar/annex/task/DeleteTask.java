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

	public DeleteTask( List<String> parameters ) {
		super( UpdateTask.DELETE, parameters );
	}

	@Override
	public boolean needsElevation() {
		Path source = Paths.get( getParameters().get( 0 ) );
		return !Files.isWritable( source );
	}

	@Override
	public TaskResult execute() throws Exception {
		Path source = Paths.get( getParameters().get( 0 ) );
		FileUtil.delete( source );
		return new TaskResult( this, TaskStatus.SUCCESS, source.toString() );
	}

}
