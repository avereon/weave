package com.avereon.weave.task;

import com.avereon.util.TextUtil;
import com.avereon.weave.Task;
import com.avereon.weave.TaskResult;
import com.avereon.weave.TaskStatus;
import com.avereon.weave.UpdateTask;

import java.nio.file.Paths;
import java.util.List;

public abstract class RunTask extends Task {

	protected RunTask( String task, List<String> parameters ) {
		super( task, parameters );
	}

	@Override
	public int getStepCount() {
		return 1;
	}

	@Override
	public TaskResult execute() throws Exception {
		if( getParameters().isEmpty() ) throw new Exception( "Missing working folder" );
		if( getParameters().size() < 2 ) throw new Exception( "Missing executable" );

		String message = UpdateTask.EXECUTE.equals( getCommand() ) ? "Executing" : "Launching";
		setMessage( message + " " + getParameters().get( 1 ) );

		ProcessBuilder builder = createProcessBuilder();
		startProcess( builder );

		incrementProgress();

		return new TaskResult( this, TaskStatus.SUCCESS, TextUtil.toString( builder.command(), " " ) );
	}

	protected ProcessBuilder createProcessBuilder() {
		ProcessBuilder builder = new ProcessBuilder( getParameters().subList( 1, getParameters().size() ) );
		builder.directory( Paths.get( getParameters().getFirst() ).toFile() );
		builder.redirectOutput( ProcessBuilder.Redirect.DISCARD );
		builder.redirectError( ProcessBuilder.Redirect.DISCARD );
		builder.redirectInput( ProcessBuilder.Redirect.INHERIT );
		return builder;
	}

	protected void startProcess( ProcessBuilder builder ) throws Exception {
		builder.start();
	}

}
