package com.xeomar.annex;

public class TaskResult {

	private TaskStatus status;

	private String message;

	public TaskResult( TaskStatus status ) {
		this( status, "No message" );
	}

	public TaskResult( TaskStatus status, String message ) {
		this.status = status;
		this.message = message;
	}

	public TaskStatus getStatus() {
		return status;
	}

	public String getMessage() {
		return message;
	}

	public static TaskResult parse( String string ) {
		int index = string.indexOf( ' ' );

		//TaskStatus status = TaskStatus.values()[ Integer.parseInt( string.substring( 0,index ) ) ];
		TaskStatus status = TaskStatus.valueOf( string.substring( 0,index ) );
		if( index < 0 ) {
			return new TaskResult( status );
		} else {
			return new TaskResult( status, string.substring( index + 1 ) );
		}
	}

	@Override
	public String toString() {
		return status + " " + message;
	}

}
