package com.xeomar.xevra.task;

import com.xeomar.util.TextUtil;
import com.xeomar.xevra.Task;
import com.xeomar.xevra.TaskResult;
import com.xeomar.xevra.TaskStatus;
import com.xeomar.xevra.UpdateTask;

import java.nio.file.Path;
import java.nio.file.Paths;
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

		// Determine the working folder
		Path workingFolder;
		if( getParameters().size() > 0 ) {
			workingFolder = Paths.get( getParameters().get( 0 ) ).getParent();
		} else {
			workingFolder = Paths.get( System.getProperty( "user.home" ) );
		}

		ProcessBuilder builder = new ProcessBuilder( getParameters() );
		builder.directory( workingFolder.toFile() );
		builder.redirectOutput( ProcessBuilder.Redirect.DISCARD ).redirectError( ProcessBuilder.Redirect.DISCARD );
		builder.start();

		// NOTE This task does not wait for the process to finish, this task is asynchronous

		incrementProgress();

		return new TaskResult( this, TaskStatus.SUCCESS, TextUtil.toString( builder.command(), " " ) );
	}

}
