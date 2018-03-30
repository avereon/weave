package com.xeomar.annex;

import java.util.List;

public class PauseTask extends AnnexTask {

	public PauseTask( List<String> parameters ) {
		super( UpdateFlag.PAUSE, parameters );
	}

	@Override
	public TaskResult execute() throws Exception {
		Thread.sleep( Long.parseLong( getParameters().get( 0 ) ) );
		return new TaskResult( TaskStatus.SUCCESS, "success" );
	}

}
