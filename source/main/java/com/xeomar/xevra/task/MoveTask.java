package com.xeomar.xevra.task;

import com.xeomar.xevra.AbstractUpdateTask;
import com.xeomar.xevra.TaskResult;
import com.xeomar.xevra.TaskStatus;
import com.xeomar.xevra.UpdateTask;

import java.nio.file.*;
import java.util.List;

public class MoveTask extends AbstractUpdateTask {

	private String message;

	private Path source;

	private Path target;

	public MoveTask( List<String> parameters ) {
		super( UpdateTask.MOVE, parameters );
		this.source = Paths.get( getParameters().get( 0 ) );
		this.target = Paths.get( getParameters().get( 1 ) );
		this.message = "Move " + this.source;
	}

	@Override
	public int getStepCount() {
		return 1;
	}

	@Override
	public void validate() {
		if( !Files.exists( source ) ) throw new IllegalArgumentException( "Source does not exist: " + source );
		if( Files.exists( target ) ) throw new IllegalArgumentException( "Target already exists: " + target );
	}

	@Override
	public boolean needsElevation() {
		boolean sourceOk = Files.isReadable( source ) && Files.isWritable( source );
		boolean targetOk = !Files.exists( target );
		return !(sourceOk & targetOk);
	}

	@Override
	public TaskResult execute() throws Exception {
		setMessage( message );
		Files.createDirectories( target.getParent() );
		Files.move( source, target );
		incrementProgress();
		return new TaskResult( this, TaskStatus.SUCCESS, "Moved: " + source + " to " + target );
	}

}
