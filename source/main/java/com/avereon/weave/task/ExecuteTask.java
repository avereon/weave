package com.avereon.weave.task;

import com.avereon.util.TextUtil;
import com.avereon.weave.Task;
import com.avereon.weave.TaskResult;
import com.avereon.weave.TaskStatus;
import com.avereon.weave.UpdateTask;

import java.nio.file.Paths;
import java.util.List;

public class ExecuteTask extends Task {

	public ExecuteTask( List<String> parameters ) {
		super( UpdateTask.EXECUTE, parameters );
	}

	@Override
	public int getStepCount() {
		return 1;
	}

	@Override
	public TaskResult execute() throws Exception {
		if( getParameters().size() < 1 ) throw new Exception( "Missing working folder" );
		if( getParameters().size() < 2 ) throw new Exception( "Missing executable" );

		setMessage( "Executing " + getParameters().get( 1 ) );

		// TODO Should this be in a retry loop?

		ProcessBuilder builder = new ProcessBuilder( getParameters().subList( 1, getParameters().size() ) );
		builder.directory( Paths.get( getParameters().get( 0 ) ).toFile() );
		builder.redirectOutput( ProcessBuilder.Redirect.DISCARD ).redirectError( ProcessBuilder.Redirect.DISCARD );
		builder.start().waitFor();

		incrementProgress();

		return new TaskResult( this, TaskStatus.SUCCESS, TextUtil.toString( builder.command(), " " ) );
	}

}
