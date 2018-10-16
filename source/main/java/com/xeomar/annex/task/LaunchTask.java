package com.xeomar.annex.task;

import com.xeomar.annex.AnnexTask;
import com.xeomar.annex.TaskResult;
import com.xeomar.annex.TaskStatus;
import com.xeomar.annex.UpdateTask;
import com.xeomar.util.TextUtil;

import java.util.List;

public class LaunchTask extends AnnexTask {

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
