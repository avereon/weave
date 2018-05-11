package com.xeomar.annex.task;

import com.xeomar.annex.Program;
import com.xeomar.annex.TaskResult;
import com.xeomar.annex.TaskStatus;
import com.xeomar.annex.UpdateTask;
import com.xeomar.util.FileUtil;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

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
		assertTrue( !Files.exists( targetRoot ) );

		try {
			String sourcePath = sourceRoot.toString();
			String targetPath = targetRoot.toString();
			List<TaskResult> results = new Program().runTasksFromString( UpdateTask.MOVE + " " + sourcePath + " " + targetPath );

			assertTaskResult( results.get( 0 ), TaskStatus.SUCCESS );
			assertThat( results.get( 0 ).getMessage(), startsWith( "Moved:" ) );
			assertThat( results.get( 0 ).getMessage(), endsWith( targetRoot.getFileName().toString() ) );

			assertTrue( !Files.exists( sourceRoot ) );
			assertTrue( Files.exists( targetRoot ) );
		} finally {
			FileUtil.delete( targetRoot );
			FileUtil.delete( sourceRoot );
		}
	}

	@Test
	public void testInvalidSource() throws Exception {
		String source = "invalidsource";
		String target = "invalidtarget";
		List<TaskResult> results = new Program().runTasksFromString( UpdateTask.MOVE + " " + source + " " + target );
		assertTaskResult( results.get( 0 ), TaskStatus.FAILURE, "IllegalArgumentException: Source does not exist: invalidsource" );
	}

}
