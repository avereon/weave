package com.xeomar.annex.task;

import com.xeomar.annex.AnnexTask;
import com.xeomar.annex.TaskResult;
import com.xeomar.annex.TaskStatus;
import com.xeomar.annex.UpdateTask;
import com.xeomar.util.LogUtil;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.Set;

public class PermissionsTask extends AnnexTask {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	public PermissionsTask( List<String> parameters ) {
		super( UpdateTask.PERMISSIONS, parameters );
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
		Set<PosixFilePermission> permissions = getPosixPermissions( getParameters().get( 0 ) );

		int size = getParameters().size();
		for( int index = 1; index < size; index++ ) {
			Path file = Paths.get( getParameters().get( index ) );
			Files.getFileAttributeView( file, PosixFileAttributeView.class ).setPermissions( permissions );
			incrementProgress();
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
		log.debug( "Parse permissions: " + permissions );
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
