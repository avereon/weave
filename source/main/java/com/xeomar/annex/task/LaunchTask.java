package com.xeomar.annex.task;

import com.xeomar.annex.AnnexTask;
import com.xeomar.annex.TaskResult;
import com.xeomar.annex.TaskStatus;
import com.xeomar.annex.UpdateTask;
import com.xeomar.util.TextUtil;

import java.util.List;

public class LaunchTask extends AnnexTask {

	public LaunchTask( List<String> parameters ) {
		super( UpdateTask.LAUNCH, parameters );
	}

	@Override
	public TaskResult execute() throws Exception {
		ProcessBuilder builder = new ProcessBuilder().inheritIO();
		builder.command().addAll( getParameters() );
		builder.start();

		// TODO Without waiting for the process to finish, this task is asynchronous

		return new TaskResult( this, TaskStatus.SUCCESS, TextUtil.toString( builder.command(), " " ) );
	}

}
