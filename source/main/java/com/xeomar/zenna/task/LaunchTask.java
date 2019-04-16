package com.xeomar.zenna.task;

import com.xeomar.util.TextUtil;
import com.xeomar.zenna.Task;
import com.xeomar.zenna.TaskResult;
import com.xeomar.zenna.TaskStatus;
import com.xeomar.zenna.UpdateTask;

import java.nio.file.Paths;
import java.util.List;

/**
 * Asynchronously start a process. Unlike the ExecuteTask, this task does not
 * wait for the process to terminate before returning.
 *
 * Parameter 0 - The process working folder
 * Parameter 1 - The executable name or path
 * Parameter + - Parameters for the executable
 */
public class LaunchTask extends Task {

	public LaunchTask( List<String> parameters ) {
		super( UpdateTask.LAUNCH, parameters );
	}

	@Override
	public int getStepCount() {
		return 1;
	}

	@Override
	public TaskResult execute() throws Exception {
		if( getParameters().size() < 1 ) throw new Exception( "Missing working folder" );
		if( getParameters().size() < 2 ) throw new Exception( "Missing executable" );

		setMessage( "Launching " + getParameters().get( 1 ) );

		ProcessBuilder builder = new ProcessBuilder( getParameters().subList( 1, getParameters().size() ) );
		builder.directory( Paths.get( getParameters().get( 0 ) ).toFile() );
		builder.redirectOutput( ProcessBuilder.Redirect.DISCARD ).redirectError( ProcessBuilder.Redirect.DISCARD );
		builder.start();
		// NOTE This task does not wait for the process to finish, this task is asynchronous

		incrementProgress();

		return new TaskResult( this, TaskStatus.SUCCESS, TextUtil.toString( builder.command(), " " ) );
	}

}
