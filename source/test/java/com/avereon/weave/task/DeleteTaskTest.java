package com.avereon.weave.task;

import com.avereon.weave.TaskResult;
import com.avereon.weave.TaskStatus;
import com.avereon.weave.UpdateTask;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DeleteTaskTest extends TaskTest {

	private String filePath = "target/deleteme";

	@Test
	public void testNeedsElevation() throws Exception {
		Path path = Paths.get( filePath );
		Files.deleteIfExists( path );
		assertFalse( Files.exists( path ) );

		DeleteTask task = new DeleteTask( List.of( filePath ) );
		assertFalse( task.needsElevation() );

		Files.createFile( path );
		assertTrue( Files.exists( path ) );
		assertFalse( task.needsElevation() );

		Files.setPosixFilePermissions( path, Set.of( PosixFilePermission.OWNER_READ ) );
		assertTrue( Files.exists( path ) );
		assertTrue( task.needsElevation() );
		Files.deleteIfExists( path );
	}

	@Test
	public void testExecute() throws Exception {
		Path path = Paths.get( filePath );
		Files.createFile( path );
		assertTrue( Files.exists( path ) );

		List<TaskResult> results = program.runTasksFromString( UpdateTask.DELETE + " \"" + filePath + "\"" );
		assertTaskResult( results.get( 0 ), TaskStatus.SUCCESS );

		assertThat( results.get( 0 ).getTask().getParameters().get( 0 ), is( filePath ) );
	}

}
