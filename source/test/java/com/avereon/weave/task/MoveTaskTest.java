package com.avereon.weave.task;

import com.avereon.util.FileUtil;
import com.avereon.weave.TaskResult;
import com.avereon.weave.TaskStatus;
import com.avereon.weave.UpdateTask;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MoveTaskTest extends TaskTest {

	@Test
	public void testConstructor() {
		String source = "source";
		String target = "target";
		MoveTask task = new MoveTask( Arrays.asList( source, target ) );
		assertThat( task.getParameters().get( 0 ), is( source ) );
		assertThat( task.getParameters().get( 1 ), is( target ) );
		assertThat( task.getParameters().size(), is( 2 ) );
	}

	@Test
	public void testExecute() throws Exception {
		Path sourceRoot = Files.createTempDirectory( getClass().getSimpleName() );
		Path targetRoot = Files.createTempDirectory( getClass().getSimpleName() );
		FileUtil.delete( targetRoot );

		assertTrue( Files.exists( sourceRoot ) );
		assertFalse( Files.exists( targetRoot ) );

		try {
			String sourcePath = sourceRoot.toString();
			String targetPath = targetRoot.toString();
			List<TaskResult> results = program.runTasksFromString( UpdateTask.MOVE + " " + sourcePath + " " + targetPath );

			assertTaskResult( results.get( 0 ), TaskStatus.SUCCESS );
			assertThat( results.get( 0 ).getMessage(), startsWith( "Moved:" ) );
			assertThat( results.get( 0 ).getMessage(), endsWith( targetRoot.getFileName().toString() ) );

			assertFalse( Files.exists( sourceRoot ) );
			assertTrue( Files.exists( targetRoot ) );
		} finally {
			FileUtil.delete( targetRoot );
			FileUtil.delete( sourceRoot );
		}
	}

	@Test
	public void testInvalidSource() throws Exception {
		String source = "missing-source";
		String target = "not-a-real-target";
		List<TaskResult> results = program.runTasksFromString( UpdateTask.MOVE + " " + source + " " + target );
		assertTaskResult( results.get( 0 ), TaskStatus.SUCCESS );
		assertThat( results.get(0).getMessage(), startsWith( "Source does not exist:" ) );
	}

}
