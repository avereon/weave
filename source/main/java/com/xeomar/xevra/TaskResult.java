package com.xeomar.xevra;

import com.xeomar.util.LogUtil;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;

public class TaskResult {

	@SuppressWarnings( "unused" )
	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private Task task;

	private TaskStatus status;

	private String message;

	public TaskResult( Task task, TaskStatus status ) {
		this( task, status, null );
	}

	public TaskResult( Task task, TaskStatus status, String message ) {
		this.task = task;
		this.status = status;
		this.message = message;
	}

	public Task getTask() {
		return task;
	}

	public TaskStatus getStatus() {
		return status;
	}

	public String getMessage() {
		return message;
	}

	public String format() {
		StringBuilder builder = new StringBuilder();
		builder.append( status.toString() );
		builder.append( " " ).append( task.getCommand() );
		if( message != null ) builder.append( " " ).append( message );
		return builder.toString();
	}

	public static TaskResult parse( Task task, String format ) {
		String status;
		String command;
		String message = "";

		String string = format.trim();

		String[] elements = string.split( " " );
		status = elements[0];
		command = elements[1];

		// Parse message
		int index = string.indexOf( command ) + command.length() + 1;
		if( index < string.length() ) message = string.substring( index );

		return new TaskResult( task, TaskStatus.valueOf( status.trim() ), message.trim() );
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if( task.isElevated() ) builder.append( "*");
		builder.append( format() );
		return builder.toString();
	}

}
