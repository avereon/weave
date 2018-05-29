package com.xeomar.annex;

import java.util.List;

public abstract class AnnexTask {

	private String command;

	private List<String> parameters;

	private String originalLine;

	public AnnexTask( String command, List<String> parameters ) {
		this.command = command;
		this.parameters = parameters;
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

	public void validate() {}

	public boolean needsElevation() {
		return false;
	}

	public String getMessage() {
		return toString();
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
