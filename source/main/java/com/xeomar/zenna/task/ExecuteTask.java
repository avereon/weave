package com.xeomar.zenna.task;

import com.xeomar.util.TextUtil;
import com.xeomar.zenna.Task;
import com.xeomar.zenna.TaskResult;
import com.xeomar.zenna.TaskStatus;
import com.xeomar.zenna.UpdateTask;

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
