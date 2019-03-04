package com.xeomar.xevra;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class Task {

	private String command;

	private List<String> parameters;

	private String originalLine;

	private boolean elevated;

	private int currentStep = 0;

	private Set<TaskListener> listeners;

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

	public String getOriginalLine() {
		return originalLine;
	}

	public void setOriginalLine( String originalLine ) {
		this.originalLine = originalLine;
	}

	public abstract int getStepCount() throws Exception;

	public void validate() {}

	public boolean needsElevation() {
		return false;
	}

	public void addTaskListener( TaskListener listener  ) {
		this.listeners.add( listener );
	}

	public void removeTaskListener( TaskListener listener ) {
		this.listeners.remove( listener );
	}

	protected void setMessage( String message ) {
		for( TaskListener listener : listeners ) {
			listener.updateMessage( message );
		}
	}

	protected int incrementProgress() {
		currentStep++;

		for( TaskListener listener : listeners ) {
			listener.updateProgress( currentStep );
		}

		return currentStep;
	}

	boolean isElevated() {
		return elevated;
	}

	void setElevated() {
		elevated = true;
	}

	public abstract TaskResult execute() throws Exception;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder( command );
		for( String parameter : parameters ) {
			builder.append( " " ).append( parameter );
		}
		return builder.toString();
	}

}
