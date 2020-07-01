package com.avereon.zenna;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class Task {

	private final String command;

	private final List<String> parameters;

	private String originalLine;

	private boolean elevated;

	private int currentStep = 0;

	private final Set<TaskListener> listeners;

	public Task( String command, List<String> parameters ) {
		this.command = command;
		this.parameters = parameters;
		this.listeners = new CopyOnWriteArraySet<>();
	}

	public String getCommand() {
		return command;
	}

	public List<String> getParameters() {
		return parameters;
	}

	public abstract int getStepCount() throws Exception;

	public void validate() {}

	public boolean needsElevation() {
		return false;
	}

	String getOriginalLine() {
		return originalLine;
	}

	void setOriginalLine( String originalLine ) {
		this.originalLine = originalLine;
	}

	void addTaskListener( TaskListener listener ) {
		this.listeners.add( listener );
	}

	void removeTaskListener( TaskListener listener ) {
		this.listeners.remove( listener );
	}

	protected void setHeader( String header ) {
		for( TaskListener listener : listeners ) {
			listener.updateHeader( header );
		}
	}

	protected void setMessage( String message ) {
		for( TaskListener listener : listeners ) {
			listener.updateMessage( message );
		}
	}

	protected void incrementProgress() {
		currentStep++;
		for( TaskListener listener : listeners ) {
			listener.updateProgress( currentStep );
		}
	}

	protected void decrementProgress() {
		currentStep--;
		for( TaskListener listener : listeners ) {
			listener.updateProgress( currentStep );
		}
	}

	boolean isElevated() {
		return elevated;
	}

	Task setElevated() {
		elevated = true;
		return this;
	}

	public abstract TaskResult execute() throws Exception;

	public TaskResult rollback() throws Exception {
		decrementProgress();
		return new TaskResult( this, TaskStatus.SUCCESS, "Rollback " + getCommand() );
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder( command );
		for( String parameter : parameters ) {
			builder.append( " " ).append( parameter );
		}
		return builder.toString();
	}

}
