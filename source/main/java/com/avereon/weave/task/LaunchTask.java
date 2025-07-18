package com.avereon.weave.task;

import com.avereon.util.ThreadUtil;
import com.avereon.weave.UpdateTask;
import lombok.CustomLog;

import java.io.IOException;
import java.util.List;

/**
 * Asynchronously start a process. Unlike the ExecuteTask, this task does not
 * wait for the process to terminate before returning. See
 * {@link UpdateTask#LAUNCH}.
 * <p>
 * Parameter 0 - The process working folder
 * Parameter 1 - The executable name or path
 * Parameter + - Parameters for the executable
 */
@CustomLog
public class LaunchTask extends RunTask {

	/**
	 * How long to wait between launch attempts. Must be smaller than {@link #TIMEOUT}.
	 */
	public static final long WAIT  = 200;

	/**
	 * How long to wait for all launch attempts. Must be larger than {@link #WAIT}.
	 */
	public static final long TIMEOUT  = 2000;

	public LaunchTask( List<String> parameters ) {
		super( UpdateTask.LAUNCH, parameters );
	}

	@Override
	protected void startProcess( ProcessBuilder builder ) throws Exception {
		Process process = null;
		IOException lastException = null;
		long timeout = System.currentTimeMillis() + TIMEOUT;
		do {
			try {
				process = builder.start();
				break;
			} catch( IOException exception ) {
				lastException = exception;
				ThreadUtil.pause( 200 );
			} finally {
				if( process == null ) ThreadUtil.pause( WAIT );
			}
		} while( System.currentTimeMillis() < timeout );

		if( process == null ) throw lastException;
	}
}
