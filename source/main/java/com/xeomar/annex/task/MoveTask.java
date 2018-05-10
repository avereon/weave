package com.xeomar.annex.task;

import com.xeomar.annex.AnnexTask;
import com.xeomar.annex.TaskResult;
import com.xeomar.annex.TaskStatus;
import com.xeomar.annex.UpdateTask;

import java.nio.file.*;
import java.util.List;

public class MoveTask extends AnnexTask {

	public MoveTask( List<String> parameters ) {
		super( UpdateTask.MOVE, parameters );
	}

	@Override
	public void validate() {
		Path source = Paths.get( getParameters().get( 0 ) );
		Path target = Paths.get( getParameters().get( 1 ) );

		if( !Files.exists( source ) ) throw new IllegalArgumentException( "Source does not exist: " + source );
		if( Files.exists( target ) ) throw new IllegalArgumentException( "Target should not exist: " + target );
		//if( !Files.isDirectory( target ) ) throw new IllegalArgumentException( "Target is not a folder: " + target );
	}

	@Override
	public boolean needsElevation() {
		Path source = Paths.get( getParameters().get( 0 ) );
		Path target = Paths.get( getParameters().get( 1 ) );

		boolean sourceOk = Files.isReadable( source ) && Files.isWritable( source );
		boolean targetOk = !Files.exists( target );

		return !(sourceOk & targetOk);
	}

	@Override
	public TaskResult execute() throws Exception {
		Path source = Paths.get( getParameters().get( 0 ) );
		Path target = Paths.get( getParameters().get( 1 ) );

		Files.move( source, target );

		return new TaskResult( this, TaskStatus.SUCCESS, "Moved: " + source + " to " + target );
	}

}
