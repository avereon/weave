package com.avereon.weave.task;

import com.avereon.util.FileUtil;
import com.avereon.weave.TaskResult;
import com.avereon.weave.TaskStatus;
import com.avereon.weave.UpdateTask;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class MoveTaskTest extends TaskTest {

	@Test
	public void testConstructor() {
		String source = "source";
		String target = "target";
		MoveTask task = new MoveTask( Arrays.asList( source, target ) );
		assertThat( task.getParameters().get( 0 ) ).isEqualTo( source );
		assertThat( task.getParameters().get( 1 ) ).isEqualTo( target );
		assertThat( task.getParameters().size() ).isEqualTo( 2 );
	}

	@Test
	public void testNeedsValidationWithNonReadableSource() throws Exception {
		Path source = Files.createTempDirectory( getClass().getSimpleName() );
		Path target = Files.createTempDirectory( getClass().getSimpleName() );

		try {
			Files.setPosixFilePermissions( source, Set.of() );
			assertThat( Files.isReadable( source ) ).isFalse();

			MoveTask task = new MoveTask( Arrays.asList( source.toString(), target.toString() ) );
			assertThat( task.needsElevation() ).isTrue();
		} finally {
			Files.setPosixFilePermissions( source, PosixFilePermissions.fromString( "rw-------" ) );
			FileUtil.delete( target );
			FileUtil.delete( source );
		}
	}

	@Test
	public void testNeedsValidationWithNonWritableSource() throws Exception {
		Path source = Files.createTempDirectory( getClass().getSimpleName() );
		Path target = Files.createTempDirectory( getClass().getSimpleName() );

		try {
			Files.setPosixFilePermissions( target, Set.of() );
			assertThat( Files.isWritable( target ) ).isFalse();

			MoveTask task = new MoveTask( Arrays.asList( source.toString(), target.toString() ) );
			assertThat( task.needsElevation() ).isTrue();
		} finally {
			Files.setPosixFilePermissions( target, PosixFilePermissions.fromString( "rw-------" ) );
			FileUtil.delete( target );
			FileUtil.delete( source );
		}
	}

	@Test
	public void testNeedsValidationWithNonWritableSourceParent() throws Exception {
		Path source = Files.createTempDirectory( getClass().getSimpleName() );
		Path parent = Files.createTempDirectory( getClass().getSimpleName() );
		Path target = parent.resolve( "target" );

		try {
			Files.setPosixFilePermissions( parent, Set.of() );
			assertThat( Files.isWritable( parent ) ).isFalse();

			MoveTask task = new MoveTask( Arrays.asList( source.toString(), target.toString() ) );
			assertThat( task.needsElevation() ).isTrue();
		} finally {
			Files.setPosixFilePermissions( parent, PosixFilePermissions.fromString( "rw-------" ) );
			FileUtil.delete( parent );
			FileUtil.delete( source );
		}
	}

	//	@Test
	//	public void testNeedsValidationWithNonWritableSourceParent() throws Exception {
	//		Path source = Files.createTempDirectory( getClass().getSimpleName() );
	//		Path parent = Files.createTempDirectory( getClass().getSimpleName() );
	//		Path target = parent.resolve( "target" );
	//
	//		try {
	//			Files.setPosixFilePermissions( parent, Set.of() );
	//			assertThat( Files.isWritable( parent ) ).isFalse();
	//
	//			MoveTask task = new MoveTask( Arrays.asList( source.toString(), target.toString() ) );
	//			assertThat( task.needsElevation() ).isTrue();
	//		} finally {
	//			Files.setPosixFilePermissions( parent, PosixFilePermissions.fromString( "rw-------" ) );
	//			FileUtil.delete( parent );
	//			FileUtil.delete( source );
	//		}
	//	}

	@Test
	public void testNeedsValidationWithNonWritableTarget() throws Exception {
		Path source = Files.createTempDirectory( getClass().getSimpleName() );
		Path target = Files.createTempDirectory( getClass().getSimpleName() );

		try {
			Files.setPosixFilePermissions( target, Set.of() );
			assertThat( Files.isWritable( target ) ).isFalse();

			MoveTask task = new MoveTask( Arrays.asList( source.toString(), target.toString() ) );
			assertThat( task.needsElevation() ).isTrue();
		} finally {
			Files.setPosixFilePermissions( target, PosixFilePermissions.fromString( "rw-------" ) );
			FileUtil.delete( target );
			FileUtil.delete( source );
		}
	}

	@Test
	public void testNeedsValidationWithNonWritableTargetParent() throws Exception {
		Path source = Files.createTempDirectory( getClass().getSimpleName() );
		Path parent = Files.createTempDirectory( getClass().getSimpleName() );
		Path target = parent.resolve( "target" );

		try {
			Files.setPosixFilePermissions( parent, Set.of() );
			assertThat( Files.isWritable( parent ) ).isFalse();

			MoveTask task = new MoveTask( Arrays.asList( source.toString(), target.toString() ) );
			assertThat( task.needsElevation() ).isTrue();
		} finally {
			Files.setPosixFilePermissions( parent, PosixFilePermissions.fromString( "rw-------" ) );
			FileUtil.delete( parent );
			FileUtil.delete( source );
		}
	}

	@Test
	public void testExecute() throws Exception {
		Path source = Files.createTempDirectory( getClass().getSimpleName() );
		Path target = Files.createTempDirectory( getClass().getSimpleName() );
		FileUtil.delete( target );

		assertThat( Files.exists( source ) ).isTrue();
		assertThat( Files.exists( target ) ).isFalse();

		try {
			String sourcePath = source.toString();
			String targetPath = target.toString();
			List<TaskResult> results = program.runTasksFromString( UpdateTask.MOVE + " " + sourcePath + " " + targetPath );

			assertTaskResult( results.get( 0 ), TaskStatus.SUCCESS );
			assertThat( results.get( 0 ).getMessage() ).startsWith( "Moved:" );
			assertThat( results.get( 0 ).getMessage() ).endsWith( target.getFileName().toString() );

			assertThat( Files.exists( source ) ).isFalse();
			assertThat( Files.exists( target ) ).isTrue();
		} finally {
			FileUtil.delete( target );
			FileUtil.delete( source );
		}
	}

	@Test
	public void testInvalidSource() throws Exception {
		String source = "missing-source";
		String target = "not-a-real-target";
		List<TaskResult> results = program.runTasksFromString( UpdateTask.MOVE + " " + source + " " + target );
		assertTaskResult( results.get( 0 ), TaskStatus.SUCCESS );
		assertThat( results.get( 0 ).getMessage() ).startsWith( "Source does not exist:" );
	}

}
