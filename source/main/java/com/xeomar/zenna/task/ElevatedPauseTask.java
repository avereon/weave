package com.xeomar.zenna.task;

import com.xeomar.util.OperatingSystem;

import java.util.List;

public class ElevatedPauseTask extends PauseTask{

	public ElevatedPauseTask( List<String> parameters ) {
		super( parameters );
	}

	@Override
	public boolean needsElevation() {
		return !OperatingSystem.isProcessElevated();
	}

}
