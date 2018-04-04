package com.xeomar.annex;

import com.xeomar.util.FileUtil;
import com.xeomar.util.IdGenerator;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

public class UpdateTaskTest {

	@Test
	public void testConstructor() {
		String source = "source";
		String target = "target";
		LaunchTask task = new LaunchTask( Arrays.asList( source, target ) );
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

		// Set up the update target
		Path targetRoot = Files.createTempDirectory( getClass().getSimpleName() );
		Path targetFile1 = Files.createTempFile( targetRoot, getClass().getSimpleName(), "" );
		Path targetSubFolder = Files.createTempDirectory( targetRoot, getClass().getSimpleName() );
		Path targetFile2 = Files.createTempFile( targetSubFolder, getClass().getSimpleName(), "" );

		// Set up the update source
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

		// Create the update package
		Path sourceZip = Paths.get( sourceRoot.toString() + ".zip" );

		try {
			// Create the update source
			FileUtil.zip( sourceRoot, sourceZip );

			// Verify the target values before executing the task
			assertThat( FileUtil.load( targetFile1 ), is( targetData1 ) );
			assertThat( FileUtil.load( targetFile2 ), is( targetData2 ) );
			assertThat( Files.exists( targetFile3 ), is( false ) );
			assertThat( Files.exists( targetFile4 ), is( false ) );

			// Execute the update task
			String sourceZipPath = sourceZip.toAbsolutePath().toString();
			String targetRootPath = targetRoot.toAbsolutePath().toString();
			TaskResult result = TaskResult.parse( new Program().runTasksFromString( UpdateFlag.UPDATE + " " + sourceZipPath + " " + targetRootPath ) );

			// Verify the result
			assertThat( result.getStatus(), is( TaskStatus.SUCCESS ) );
			assertThat( result.getMessage(), startsWith( "Updated:" ) );
			assertThat( result.getMessage(), endsWith( targetRoot.getFileName().toString() ) );

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
		String source = "/invalidsource";
		String target = "/invalidtarget";
		TaskResult result = TaskResult.parse( new Program().runTasksFromString( UpdateFlag.UPDATE + " " + source + " " + target ) );
		assertThat( result.getStatus(), is( TaskStatus.FAILURE ) );
		assertThat( result.getMessage(), startsWith( "IllegalArgumentException: Source not found" ) );
	}

	@Test
	public void testInvalidTarget() throws Exception {
		String source = new File( "" ).getCanonicalPath();
		String target = "/invalidtarget";
		TaskResult result = TaskResult.parse( new Program().runTasksFromString( UpdateFlag.UPDATE + " " + source + " " + target ) );
		assertThat( result.getStatus(), is( TaskStatus.FAILURE ) );
		assertThat( result.getMessage(), startsWith( "IllegalArgumentException: Target not found" ) );
	}

}
