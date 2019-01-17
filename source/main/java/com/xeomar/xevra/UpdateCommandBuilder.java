package com.xeomar.xevra;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UpdateCommandBuilder {

	private List<List<String>> lines;

	public UpdateCommandBuilder( String... commands ) {
		lines = new ArrayList<>();
		line();
		add( commands );
	}

	public UpdateCommandBuilder add( String... commands ) {
		return add( Arrays.asList( commands ) );
	}

	public UpdateCommandBuilder add( List<String> commands ) {
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
