package com.xeomar.xevra.task;

import com.xeomar.util.TextUtil;
import com.xeomar.xevra.Task;
import com.xeomar.xevra.TaskResult;
import com.xeomar.xevra.TaskStatus;
import com.xeomar.xevra.UpdateTask;

import java.util.List;

public class LaunchTask extends Task {

	private String message;

	public LaunchTask( List<String> parameters ) {
		super( UpdateTask.LAUNCH, parameters );
		this.message = "Launch " + getParameters().get( 0 );
	}

	@Override
	public int getStepCount() {
		return 1;
	}

	@Override
	public TaskResult execute() throws Exception {
		setMessage( message );

		ProcessBuilder builder = new ProcessBuilder( getParameters() );
		builder.redirectOutput( ProcessBuilder.Redirect.DISCARD ).redirectError( ProcessBuilder.Redirect.DISCARD );
		builder.start();

		// NOTE This task does not wait for the process to finish, this task is asynchronous

		incrementProgress();

		return new TaskResult( this, TaskStatus.SUCCESS, TextUtil.toString( builder.command(), " " ) );
	}

}
