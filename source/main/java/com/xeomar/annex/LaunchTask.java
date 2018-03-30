package com.xeomar.annex;

import com.xeomar.util.TextUtil;

import java.util.List;

public class LaunchTask extends AnnexTask {

	public LaunchTask( List<String> parameters ) {
		super( UpdateFlag.LAUNCH, parameters );
	}

	@Override
	public TaskResult execute() throws Exception {
		ProcessBuilder builder = new ProcessBuilder();
		builder.command().addAll( getParameters() );
		Process process = builder.start();

		// TODO Without waiting for the process to finish, this task is asynchronous

		String commands = TextUtil.toString( builder.command() );
		return new TaskResult( TaskStatus.SUCCESS, commands.substring( 1, commands.length() - 1 ) );
	}

}
