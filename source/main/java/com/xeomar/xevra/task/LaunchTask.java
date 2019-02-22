package com.xeomar.xevra.task;

import com.xeomar.xevra.AbstractUpdateTask;
import com.xeomar.xevra.TaskResult;
import com.xeomar.xevra.TaskStatus;
import com.xeomar.xevra.UpdateTask;
import com.xeomar.util.TextUtil;

import java.util.List;

public class LaunchTask extends AbstractUpdateTask {

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

		ProcessBuilder builder = new ProcessBuilder().inheritIO();
		builder.command().addAll( getParameters() );
		builder.start();

		// NOTE This task does not wait for the process to finish, this task is asynchronous

		incrementProgress();

		return new TaskResult( this, TaskStatus.SUCCESS, TextUtil.toString( builder.command(), " " ) );
	}

}
