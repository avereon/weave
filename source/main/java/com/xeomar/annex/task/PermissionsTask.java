package com.xeomar.annex.task;

import com.xeomar.annex.AnnexTask;
import com.xeomar.annex.TaskResult;
import com.xeomar.annex.TaskStatus;
import com.xeomar.annex.UpdateTask;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.Set;

public class PermissionsTask extends AnnexTask {

	public PermissionsTask( List<String> parameters ) {
		super( UpdateTask.PERMISSIONS, parameters );
	}

	@Override
	public TaskResult execute() throws Exception {
		// The first parameter should be a permissions mask in octal or rwx format
		if( getParameters().size() < 1 ) return new TaskResult( this, TaskStatus.FAILURE, "Missing permission mask" );

		Set<PosixFilePermission> permissions = getPosixPermissions( getParameters().get( 0 ) );

		int size = getParameters().size();
		for( int index = 1; index < size; index++ ) {
			Path file = Paths.get( getParameters().get( index ) );
			Files.getFileAttributeView( file, PosixFileAttributeView.class ).setPermissions( permissions );
		}

		return new TaskResult( this, TaskStatus.SUCCESS );
	}

	private Set<PosixFilePermission> getPosixPermissions( String permissions ) {
		// If there are four numbers
		if( permissions.length() == 4 ) permissions = permissions.substring( 1 );

		// If there are three numbers
		if( permissions.length() == 3 ) {
			StringBuilder builder = new StringBuilder();
			for( char c : permissions.toCharArray() ) {
				builder.append( toString( ((int)c) - 48 ) );
			}
			permissions = builder.toString();
		}

		// If there are nine letters
		return PosixFilePermissions.fromString( permissions );
	}

	private String toString( int value ) {
		switch( value ) {
			case 7: {
				return "rwx";
			}
			case 6: {
				return "rw-";
			}
			case 5: {
				return "r-x";
			}
			case 4: {
				return "r--";
			}
			case 3: {
				return "-wx";
			}
			case 2: {
				return "-w-";
			}
			case 1: {
				return "--x";
			}
			default: {
				return "---";
			}
		}
	}

}
