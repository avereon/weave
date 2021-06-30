package com.avereon.weave.task;

import com.avereon.weave.Task;
import com.avereon.weave.TaskResult;
import com.avereon.weave.TaskStatus;
import com.avereon.weave.UpdateTask;
import lombok.extern.flogger.Flogger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * The permissions task allows permissions to be set on one or more files. The
 * same permissions are set on all files specified.
 * <p>
 * Parameter 0: The permissions bitmask. Example: 755
 */
@Flogger
public class PermissionsTask extends Task {

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
		boolean userReadOnly = !isRead( world );
		boolean userWrite = isWrite( user );
		boolean userWriteOnly = !isWrite( world );
		boolean userExec = isExec( user );
		boolean userExecOnly = !isExec( world );

		int size = getParameters().size();
		for( int index = 1; index < size; index++ ) {
			Path path = Paths.get( getParameters().get( index ) );

			// Skip missing files
			if( !Files.exists( path ) ) continue;

			log.atFine().log( "Setting permission on: %s", path );

			File file = path.toFile();
			boolean read = file.setReadable( userRead, userReadOnly );
			boolean write = file.setWritable( userWrite, userWriteOnly );
			boolean exec = file.setExecutable( userExec, userExecOnly );

			if( !(read & write & exec) ) return new TaskResult( this, TaskStatus.FAILURE, "Unable to set permission on: " + path );

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
