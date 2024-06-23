package com.avereon.weave.task;

import com.avereon.weave.UpdateTask;
import lombok.CustomLog;

import java.util.List;

/**
 * Synchronously start a process. Unlike the LaunchTask, this task waits
 * for the process to terminate before continuing. See
 * {@link UpdateTask#EXECUTE}.
 * <p>
 * Parameter 0 - The process working folder
 * Parameter 1 - The executable name or path
 * Parameter + - Parameters for the executable
 */
@CustomLog
public class ExecuteTask extends RunTask {

	public ExecuteTask( List<String> parameters ) {
		super( UpdateTask.EXECUTE, parameters );
	}

	protected void startProcess(ProcessBuilder builder) throws Exception {
		builder.start().waitFor();
	}

}
