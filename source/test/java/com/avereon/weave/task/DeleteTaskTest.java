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

import static org.assertj.core.api.Assertions.assertThat;

public class DeleteTaskTest extends TaskTest {

	private final String filePath = "target/deleteme";

	@Test
	public void testNeedsElevation() throws Exception {
		Path path = Paths.get( filePath );
		Files.deleteIfExists( path );
		assertThat( Files.exists( path ) ).isFalse();

		DeleteTask task = new DeleteTask( List.of( filePath ) );
		assertThat( task.needsElevation() ).isFalse();

		Files.createFile( path );
		assertThat( Files.exists( path ) ).isTrue();
		assertThat( task.needsElevation() ).isFalse();

		Files.setPosixFilePermissions( path, Set.of( PosixFilePermission.OWNER_READ ) );
		assertThat( Files.exists( path ) ).isTrue();
		assertThat( task.needsElevation() ).isTrue();
		Files.deleteIfExists( path );
	}

	@Test
	public void testExecute() throws Exception {
		Path path = Paths.get( filePath );
		Files.createFile( path );
		assertThat( Files.exists( path ) ).isTrue();

		List<TaskResult> results = program.runTasksFromString( UpdateTask.DELETE + " \"" + filePath + "\"" );
		assertTaskResult( results.get( 0 ), TaskStatus.SUCCESS );

		assertThat( results.get( 0 ).getTask().getParameters().get( 0 ) ).isEqualTo( filePath );
	}

}
