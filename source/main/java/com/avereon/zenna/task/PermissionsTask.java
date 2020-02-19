package com.avereon.zenna.task;

import com.avereon.util.Log;
import com.avereon.zenna.Task;
import com.avereon.zenna.TaskResult;
import com.avereon.zenna.TaskStatus;
import com.avereon.zenna.UpdateTask;

import java.lang.System.Logger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * The permissions task allows permissions to be set on one or more files. The
 * same permissions are set on all files specified.
 * <p>
 * Parameter 0: The permissions bitmask. Example: 700
 */
public class PermissionsTask extends Task {

	private static final Logger log = Log.get();

	public PermissionsTask( List<String> parameters ) {
		super( UpdateTask.PERMISSIONS, parameters );
	}

	@Override
	public int getStepCount() {
		return getParameters().size() - 1;
	}

	@Override
	public void validate() {
		if( getParameters().size() < 1 ) throw new IllegalArgumentException( "Missing permission mask" );
	}

	@Override
	public boolean needsElevation() {
		// Check all the files to see if they are writable without elevation
		int size = getParameters().size();
		for( int index = 1; index < size; index++ ) {
			Path file = Paths.get( getParameters().get( index ) );
			if( Files.exists( file ) && !Files.isWritable( file ) ) return true;
		}
		return false;
	}

	@Override
	public TaskResult execute() throws Exception {
		setMessage( "Setting permissions" );

		String permissions = getParameters().get( 0 );
		if( permissions.length() == 4 ) permissions = permissions.substring( 1 );

		int user = getUserPermissions( permissions );
		int group = getGroupPermissions( permissions );
		int world = getWorldPermissions( permissions );

		boolean userRead = isRead( user );
		boolean worldRead = userRead && isRead( world );
		boolean userWrite = isWrite( user );
		boolean worldWrite = userWrite && isWrite( world );
		boolean userExec = isExec( user );
		boolean worldExec = userExec && isExec( world );

		int size = getParameters().size();
		for( int index = 1; index < size; index++ ) {
			Path file = Paths.get( getParameters().get( index ) );

			// Skip missing files
			if( !Files.exists( file ) ) continue;
			log.log( Log.DEBUG, "Setting permission on: " + file );

			boolean read = file.toFile().setReadable( userRead, worldRead );
			boolean write = file.toFile().setWritable( userWrite, worldWrite );
			boolean exec = file.toFile().setExecutable( userExec, worldExec );

			if( !(read & write & exec) ) return new TaskResult( this, TaskStatus.FAILURE, "Unable to set permission on: " + file );

			incrementProgress();
		}

		return new TaskResult( this, TaskStatus.SUCCESS );
	}

	int getUserPermissions( String permissions ) {
		return permissions.charAt( 0 ) - '0';
	}

	int getGroupPermissions( String permissions ) {
		return permissions.charAt( 1 ) - '0';
	}

	int getWorldPermissions( String permissions ) {
		return permissions.charAt( 2 ) - '0';
	}

	boolean isRead( int value ) {
		return (value & 4) != 0;
	}

	boolean isWrite( int value ) {
		return (value & 2) != 0;
	}

	boolean isExec( int value ) {
		return (value & 1) != 0;
	}

}
