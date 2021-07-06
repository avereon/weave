package com.avereon.weave.task;

import com.avereon.util.FileUtil;
import com.avereon.weave.Task;
import com.avereon.weave.TaskResult;
import com.avereon.weave.TaskStatus;
import com.avereon.weave.UpdateTask;
import lombok.CustomLog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@CustomLog
public class MoveTask extends Task {

	private final Path source;

	private final Path target;

	public MoveTask( List<String> parameters ) {
		super( UpdateTask.MOVE, parameters );
		this.source = Paths.get( getParameters().get( 0 ) );
		this.target = Paths.get( getParameters().get( 1 ) );
	}

	@Override
	public int getStepCount() {
		return 1;
	}

	@Override
	public void validate() {
		if( !Files.exists( source ) ) log.atWarning().log( "Source does not exist: %s", source );
		if( Files.exists( target ) ) throw new IllegalArgumentException( "Target already exists: " + target );
	}

	@Override
	public void prerequisites() throws Exception {
		// Try to create missing folders without elevation
		if( target != null ) {
			Path parent = target.getParent();
			if( parent != null ) {
				Path existingParent = FileUtil.findValidParent( target );
				if( parent != existingParent ) Files.createDirectories( parent );
			}
		}
	}

	@Override
	public boolean needsElevation() {
		if( !Files.exists( source ) ) return false;

		// Find the source parent
		Path sourceParent = FileUtil.findValidParent( source );

		// Find the existing target parent
		Path targetParent = FileUtil.findValidParent( target );

		boolean sourceOk = (Files.isReadable( source ) && Files.isWritable( source )) && Files.isWritable( sourceParent );
		boolean targetOk = (!Files.exists( target ) || Files.isWritable( target )) && Files.isWritable( targetParent );

		return !(sourceOk & targetOk);
	}

	@Override
	public TaskResult execute() throws Exception {
		System.err.println( "Executing move task: " + source );
		TaskResult result = move( source, target );
		incrementProgress();
		return result;
	}

	@Override
	public TaskResult rollback() throws Exception {
		System.err.println( "Rollback move task: " + source );
		TaskResult result = move( target, source );
		decrementProgress();
		return result;
	}

	private TaskResult move( Path source, Path target ) throws IOException {
		setMessage( "Move " + source );
		TaskResult result;
		if( Files.exists( source ) ) {
			// FIXME creating the folders here can cause some problems when running elevated
			Files.createDirectories( target.getParent() );
			Files.move( source, target );
			result = new TaskResult( this, TaskStatus.SUCCESS, "Moved: " + source + " to " + target );
		} else {
			result = new TaskResult( this, TaskStatus.SUCCESS, "Source does not exist: " + source );
		}
		return result;
	}

}
