package com.xeomar.zenna.task;

import com.xeomar.util.OperatingSystem;

import java.util.List;

public class ElevatedEchoTask extends EchoTask {

	public ElevatedEchoTask( List<String> parameters ) {
		super( parameters );
	}

	@Override
	public boolean needsElevation() {
		return !OperatingSystem.isProcessElevated();
	}

}
