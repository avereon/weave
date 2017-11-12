package com.xeomar.annex

import com.xeomar.razor.FileUtil
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

class UpdateTaskTest {

	@Test
	fun testConstructor() {
		val source = "source"
		val target = "target"
		val task = LaunchTask("update", Arrays.asList(source, target))
		Assert.assertThat(task.getParameters()[0], Matchers.`is`(source))
		Assert.assertThat(task.getParameters()[1], Matchers.`is`(target))
		Assert.assertThat(task.getParameters().size, Matchers.`is`(2))
	}

	@Test
	fun testExecute() {
		// Set up the update source
		val sourceRoot = Files.createTempDirectory(javaClass.simpleName)
		val sourceFile1 = Files.createTempFile(sourceRoot, javaClass.simpleName, "")
		val sourceFile2 = Files.createTempFile(sourceRoot, javaClass.simpleName, "")
		val sourceSubFolder = Files.createTempDirectory(sourceRoot, javaClass.simpleName)
		val sourceFile3 = Files.createTempFile(sourceSubFolder, javaClass.simpleName, "")
		val sourceFile4 = Files.createTempFile(sourceSubFolder, javaClass.simpleName, "")

		val sourceZip = Paths.get(sourceRoot.toString() + ".zip")

		// Set up the update target
		val targetRoot = Files.createTempDirectory(javaClass.simpleName)
		val targetFile1 = Files.createTempFile(targetRoot, javaClass.simpleName, "")
		val targetSubFolder = Files.createTempDirectory(targetRoot, javaClass.simpleName)
		val targetFile2 = Files.createTempFile(targetSubFolder, javaClass.simpleName, "")

		// NEXT Store random data and collect hash codes

		try {
			// Create the update source
			FileUtil.zip(sourceRoot, sourceZip)

			// Create the update task

			// Execute the update task

			// Verify the target is modified
		} finally {
			FileUtil.delete(targetRoot)
			FileUtil.delete(sourceRoot)
			FileUtil.delete(sourceZip)
		}
	}

}
