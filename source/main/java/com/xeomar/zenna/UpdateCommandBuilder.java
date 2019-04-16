package com.xeomar.zenna;

import java.util.ArrayList;
import java.util.List;

public class UpdateCommandBuilder {

	private List<List<String>> lines;

	public UpdateCommandBuilder() {
		lines = new ArrayList<>();
		lines.add( new ArrayList<>() );
	}

	public UpdateCommandBuilder add( String command, String... parameters ) {
		return add( command, List.of( parameters ) );
	}

	public UpdateCommandBuilder add( String command, List<String> parameters ) {
		List<String> commands = new ArrayList<>();
		commands.add( command );
		commands.addAll( parameters );
		lines.get( lines.size() - 1 ).addAll( commands );
		return this;
	}

	public UpdateCommandBuilder line() {
		lines.add( new ArrayList<>() );
		return this;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();

		for( List<String> line : lines ) {
			int count = line.size();
			if( count > 0 ) builder.append( line.get( 0 ) );
			for( int index = 1; index < count; index++ ) {
				builder.append( " " );
				String parameter = line.get( index );
				boolean hasSpaces = parameter.contains( " " );
				if( hasSpaces ) builder.append( "\"");
				builder.append( parameter );
				if( hasSpaces ) builder.append( "\"");
			}
			builder.append( "\n");
		}

		return builder.toString();
	}

}
