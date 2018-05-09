package com.xeomar.annex;

public class TaskResult {

	private AnnexTask task;

	private TaskStatus status;

	private String message;

	public TaskResult( AnnexTask task, TaskStatus status ) {
		this( task, status, "No message" );
	}

	public TaskResult( AnnexTask task, TaskStatus status, String message ) {
		this.task = task;
		this.status = status;
		this.message = message;
	}

	public AnnexTask getTask() {
		return task;
	}

	public TaskStatus getStatus() {
		return status;
	}

	public String getMessage() {
		return message;
	}

	public static TaskResult parse( AnnexTask task, String string ) {
		int index = string.indexOf( ' ' );
		if( index < 0 ) {
			return new TaskResult( task, TaskStatus.valueOf( string ) );
		} else {
			return new TaskResult( task, TaskStatus.valueOf( string.substring( 0, index ) ), string.substring( index + 1 ) );
		}
	}

	@Override
	public String toString() {
		return status + " " + message;
	}

}
