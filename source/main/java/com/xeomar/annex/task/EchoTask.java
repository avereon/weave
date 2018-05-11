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

public class EchoTask extends AnnexTask {

	public EchoTask( List<String> parameters ) {
		super( UpdateTask.ECHO, parameters );
	}

	@Override
	public TaskResult execute() throws Exception {
		return new TaskResult( this, TaskStatus.SUCCESS, TextUtil.toString( getParameters(), " " ) );
	}

}
