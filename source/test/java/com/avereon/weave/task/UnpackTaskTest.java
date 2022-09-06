package com.avereon.weave.task;

import com.avereon.log.Log;
import com.avereon.util.FileUtil;
import com.avereon.util.IdGenerator;
import com.avereon.util.LogFlag;
import com.avereon.util.Parameters;
import com.avereon.weave.TaskResult;
import com.avereon.weave.TaskStatus;
import com.avereon.weave.UpdateTask;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class UnpackTaskTest extends TaskTest {

	@Test
	public void testConstructor() {
		String source = "source";
		String target = "target";
		UnpackTask task = new UnpackTask( Arrays.asList( source, target ) );
		assertThat( task.getParameters().get( 0 ) ).isEqualTo( source );
		assertThat( task.getParameters().get( 1 ) ).isEqualTo( target );
		assertThat( task.getParameters().size() ).isEqualTo( 2 );
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
			assertThat( FileUtil.load( targetFile1 ) ).isEqualTo( targetData1 );
			assertThat( FileUtil.load( targetFile2 ) ).isEqualTo( targetData2 );
			assertThat( Files.exists( targetFile3 ) ).isEqualTo( false );
			assertThat( Files.exists( targetFile4 ) ).isEqualTo( false );

			// Execute the overlay task
			String sourceZipPath = sourceZip.toAbsolutePath().toString();
			String targetRootPath = targetRoot.toAbsolutePath().toString();
			List<TaskResult> results = program.runTasksFromString( UpdateTask.UNPACK + " " + sourceZipPath + " " + targetRootPath );

			// Verify the result
			assertTaskResult( results.get( 0 ), TaskStatus.SUCCESS );
			assertThat( results.get( 0 ).getMessage() ).startsWith( sourceZipPath );
			assertThat( results.get( 0 ).getMessage() ).endsWith( targetRootPath );

			// Verify the target values after executing the task
			assertThat( FileUtil.load( targetFile1 ) ).isEqualTo( sourceData1 );
			assertThat( FileUtil.load( targetFile2 ) ).isEqualTo( sourceData3 );
			assertThat( FileUtil.load( targetFile3 ) ).isEqualTo( sourceData2 );
			assertThat( FileUtil.load( targetFile4 ) ).isEqualTo( sourceData4 );
		} finally {
			FileUtil.delete( targetRoot );
			FileUtil.delete( sourceRoot );
			FileUtil.delete( sourceZip );
		}
	}

	@Test
	public void testInvalidSource() throws Exception {
		Log.configureLogging( program, Parameters.parse( LogFlag.LOG_LEVEL, "info" ) );
		String source = "invalidsource";
		String target = "invalidtarget";
		List<TaskResult> results = program.runTasksFromString( UpdateTask.UNPACK + " " + source + " " + target );
		assertTaskResult( results.get( 0 ), TaskStatus.FAILURE, "IllegalArgumentException: Source not found: invalidsource" );
	}

}
