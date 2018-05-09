package com.xeomar.annex.task;

import com.xeomar.annex.AnnexTask;
import com.xeomar.annex.TaskResult;
import com.xeomar.annex.TaskStatus;
import com.xeomar.annex.UpdateTask;
import com.xeomar.util.LogUtil;
import com.xeomar.util.TextUtil;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.List;

public class LogTask extends AnnexTask {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	public LogTask( List<String> parameters ) {
		super( UpdateTask.LOG, parameters );
	}

	@Override
	public TaskResult execute() throws Exception {
		log.info( TextUtil.toString( getParameters(), " " ) );
		return new TaskResult( this, TaskStatus.SUCCESS, "" );
	}

}
