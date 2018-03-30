package com.xeomar.annex;

import java.util.List;

public abstract class AnnexTask {

	private String command;

	private List<String> parameters;

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

	public boolean needsElevation() {
		return false;
	}

	public abstract TaskResult execute() throws Exception;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append( command );
		for( String parameter : parameters ) {
			builder.append( " " );
			builder.append( parameter );
		}
		return builder.toString();
	}

}