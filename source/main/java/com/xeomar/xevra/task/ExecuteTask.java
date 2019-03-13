package com.xeomar.xevra.task;

import com.xeomar.util.TextUtil;
import com.xeomar.xevra.Task;
import com.xeomar.xevra.TaskResult;
import com.xeomar.xevra.TaskStatus;
import com.xeomar.xevra.UpdateTask;

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

		ProcessBuilder builder = new ProcessBuilder( getParameters().subList( 1, getParameters().size() ) );
		builder.directory( Paths.get( getParameters().get( 0 ) ).toFile() );
		builder.redirectOutput( ProcessBuilder.Redirect.DISCARD ).redirectError( ProcessBuilder.Redirect.DISCARD );
		builder.start().waitFor();

		incrementProgress();

		return new TaskResult( this, TaskStatus.SUCCESS, TextUtil.toString( builder.command(), " " ) );
	}

}
