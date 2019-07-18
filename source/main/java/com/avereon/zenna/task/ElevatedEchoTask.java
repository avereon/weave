package com.avereon.zenna.task;

import com.avereon.util.OperatingSystem;

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
