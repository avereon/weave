package com.xeomar.annex.task;

import com.xeomar.annex.AnnexTask;
import com.xeomar.annex.TaskResult;
import com.xeomar.annex.UpdateTask;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
		if( !Files.exists( target ) ) throw new IllegalArgumentException( "Target does not exist: " + target );
		if( !Files.isDirectory( target ) ) throw new IllegalArgumentException( "Target is not a folder: " + target );
	}

	@Override
	public boolean needsElevation() {
		Path source = Paths.get( getParameters().get( 0 ) );
		Path target = Paths.get( getParameters().get( 1 ) );

		boolean sourceOk = Files.isReadable( source ) && Files.isWritable( source );
		boolean targetOk = Files.isWritable( target );

		return !(sourceOk & targetOk);
	}

	@Override
	public TaskResult execute() throws Exception {
		return null;
	}

}
