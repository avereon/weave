package com.avereon.zenna.task;

import com.avereon.util.*;
import com.avereon.zenna.TaskResult;
import com.avereon.zenna.TaskStatus;
import com.avereon.zenna.UpdateTask;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class UnpackTaskTest extends TaskTest {

	@Test
	public void testConstructor() {
		String source = "source";
		String target = "target";
		UnpackTask task = new UnpackTask( Arrays.asList( source, target ) );
		assertThat( task.getParameters().get( 0 ), is( source ) );
		assertThat( task.getParameters().get( 1 ), is( target ) );
		assertThat( task.getParameters().size(), is( 2 ) );
	}

	@Test
	public void testExecute() throws Exception {
		// Generate test data
		String targetData1 = IdGenerator.getId();
		String targetData2 = IdGenerator.getId();

		String sourceData1 = IdGenerator.getId();
		String sourceData2 = IdGenerator.getId();
		String sourceData3 = IdGenerator.getId();
		String sourceData4 = IdGenerator.getId();

		// Set up the overlay target
		Path targetRoot = Files.createTempDirectory( getClass().getSimpleName() );
		Path targetFile1 = Files.createTempFile( targetRoot, getClass().getSimpleName(), "" );
		Path targetSubFolder = Files.createTempDirectory( targetRoot, getClass().getSimpleName() );
		Path targetFile2 = Files.createTempFile( targetSubFolder, getClass().getSimpleName(), "" );

		// Set up the overlay source
		Path sourceRoot = Files.createTempDirectory( getClass().getSimpleName() );
		Path sourceFile1 = sourceRoot.resolve( targetFile1.getFileName() );
		Path sourceFile2 = Files.createTempFile( sourceRoot, getClass().getSimpleName(), "" );
		Path sourceSubFolder = sourceRoot.resolve( targetSubFolder.getFileName() );
		Files.createDirectories( sourceSubFolder );
		Path sourceFile3 = sourceSubFolder.resolve( targetFile2.getFileName() );
		Path sourceFile4 = Files.createTempFile( sourceSubFolder, getClass().getSimpleName(), "" );

		Path targetFile3 = targetRoot.resolve( sourceFile2.getFileName() );
		Path targetFile4 = targetSubFolder.resolve( sourceFile4.getFileName() );

		// Store test data
		FileUtil.save( targetData1, targetFile1 );
		FileUtil.save( targetData2, targetFile2 );
		FileUtil.save( sourceData1, sourceFile1 );
		FileUtil.save( sourceData2, sourceFile2 );
		FileUtil.save( sourceData3, sourceFile3 );
		FileUtil.save( sourceData4, sourceFile4 );

		// Create the overlay package
		Path sourceZip = Paths.get( sourceRoot.toString() + ".zip" );

		try {
			// Create the overlay source
			FileUtil.zip( sourceRoot, sourceZip );

			// Verify the target values before executing the task
			assertThat( FileUtil.load( targetFile1 ), is( targetData1 ) );
			assertThat( FileUtil.load( targetFile2 ), is( targetData2 ) );
			assertThat( Files.exists( targetFile3 ), is( false ) );
			assertThat( Files.exists( targetFile4 ), is( false ) );

			// Execute the overlay task
			String sourceZipPath = sourceZip.toAbsolutePath().toString();
			String targetRootPath = targetRoot.toAbsolutePath().toString();
			List<TaskResult> results = program.runTasksFromString( UpdateTask.UNPACK + " " + sourceZipPath + " " + targetRootPath );

			// Verify the result
			assertTaskResult( results.get( 0 ), TaskStatus.SUCCESS );
			assertThat( results.get( 0 ).getMessage(), startsWith( "Unpacked:" ) );
			assertThat( results.get( 0 ).getMessage(), endsWith( targetRoot.getFileName().toString() ) );

			// Verify the target values after executing the task
			assertThat( FileUtil.load( targetFile1 ), is( sourceData1 ) );
			assertThat( FileUtil.load( targetFile2 ), is( sourceData3 ) );
			assertThat( FileUtil.load( targetFile3 ), is( sourceData2 ) );
			assertThat( FileUtil.load( targetFile4 ), is( sourceData4 ) );
		} finally {
			FileUtil.delete( targetRoot );
			FileUtil.delete( sourceRoot );
			FileUtil.delete( sourceZip );
		}
	}

	@Test
	public void testInvalidSource() throws Exception {
		LogUtil.configureLogging( program, Parameters.parse( LogFlag.LOG_LEVEL, "info" ) );
		String source = "invalidsource";
		String target = "invalidtarget";
		List<TaskResult> results =  program.runTasksFromString( UpdateTask.UNPACK + " " + source + " " + target );
		assertTaskResult( results.get( 0 ), TaskStatus.FAILURE , "IllegalArgumentException: Source not found: invalidsource" );
	}

}
