package com.xeomar.xevra.task;

import com.xeomar.util.LogUtil;
import com.xeomar.xevra.AbstractUpdateTask;
import com.xeomar.xevra.TaskResult;
import com.xeomar.xevra.TaskStatus;
import com.xeomar.xevra.UpdateTask;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
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
public class PermissionsTask extends AbstractUpdateTask {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	public PermissionsTask( List<String> parameters ) {
		super( UpdateTask.PERMISSIONS, parameters );
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
	public void validate() {
		if( getParameters().size() < 1 ) throw new IllegalArgumentException( "Missing permission mask" );
	}

	@Override
	public int getStepCount() {
		return getParameters().size() - 1;
	}

	@Override
	public TaskResult execute() throws Exception {
		setMessage( "Setting permissions" );

		String permissions = getParameters().get( 0 );
		if( permissions.length() == 4 ) permissions = permissions.substring( 1 );

		int user = permissions.charAt( 0 ) - '0';
		int group = permissions.charAt( 1 ) - '0';
		int world = permissions.charAt( 2 ) - '0';

		boolean userRead = isRead( user );
		boolean worldRead = userRead && isRead( world );
		boolean userWrite = isWrite( group );
		boolean worldWrite = userWrite && isWrite( world );
		boolean userExec = isExec( user );
		boolean worldExec = userExec && isExec( world );

		int size = getParameters().size();
		for( int index = 1; index < size; index++ ) {
			Path file = Paths.get( getParameters().get( index ) );
			log.debug( "Setting permission on: " + file );

			if( Files.exists( file ) ) {
				file.toFile().setReadable( userRead, worldRead );
				file.toFile().setWritable( userWrite, worldWrite );
				file.toFile().setExecutable( userExec, worldExec );
			} else {
				log.warn( "File not found: " + file );
			}
			incrementProgress();
		}

		return new TaskResult( this, TaskStatus.SUCCESS );
	}

	//	private Set<PosixFilePermission> getPosixPermissions( String permissions ) {
	//		// If there are four numbers
	//		if( permissions.length() == 4 ) permissions = permissions.substring( 1 );
	//
	//		// If there are three numbers
	//		if( permissions.length() == 3 ) {
	//			StringBuilder builder = new StringBuilder();
	//			for( char c : permissions.toCharArray() ) {
	//				builder.append( toString( ((int)c) - 48 ) );
	//			}
	//			permissions = builder.toString();
	//		}
	//
	//		// If there are nine letters
	//		log.debug( "Parse permissions: " + permissions );
	//		return PosixFilePermissions.fromString( permissions );
	//	}

	//	private String convertFromMask( String permissions ) {
	//		// If there are four numbers
	//		if( permissions.length() == 4 ) permissions = permissions.substring( 1 );
	//
	//		// If there are three numbers
	//		if( permissions.length() == 3 ) {
	//			StringBuilder builder = new StringBuilder();
	//			for( char c : permissions.toCharArray() ) {
	//				builder.append( toString( ((int)c) - 48 ) );
	//			}
	//			permissions = builder.toString();
	//		}
	//
	//		return permissions;
	//	}

	private boolean isRead( int value ) {
		return (value & 4) != 0;
	}

	private boolean isWrite( int value ) {
		return (value & 2) != 0;
	}

	private boolean isExec( int value ) {
		return (value & 1) != 0;
	}

	//	private String toString( int value ) {
	//		switch( value ) {
	//			case 7: {
	//				return "rwx";
	//			}
	//			case 6: {
	//				return "rw-";
	//			}
	//			case 5: {
	//				return "r-x";
	//			}
	//			case 4: {
	//				return "r--";
	//			}
	//			case 3: {
	//				return "-wx";
	//			}
	//			case 2: {
	//				return "-w-";
	//			}
	//			case 1: {
	//				return "--x";
	//			}
	//			default: {
	//				return "---";
	//			}
	//		}
	//	}

}
