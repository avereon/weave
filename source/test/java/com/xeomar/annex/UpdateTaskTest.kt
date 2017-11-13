package com.xeomar.annex

import com.xeomar.util.FileUtil
import com.xeomar.util.IdGenerator
import org.hamcrest.Matchers
import org.hamcrest.Matchers.`is`
import org.junit.Assert
import org.junit.Assert.assertThat
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

class UpdateTaskTest {

	@Test
	fun testConstructor() {
		val source = "source"
		val target = "target"
		val task = LaunchTask(Arrays.asList(source, target))
		Assert.assertThat(task.parameters[0], Matchers.`is`(source))
		Assert.assertThat(task.parameters[1], Matchers.`is`(target))
		Assert.assertThat(task.parameters.size, Matchers.`is`(2))
	}

	@Test
	fun testExecute() {
		// Generate test data
		val targetData1 = IdGenerator.getId()
		val targetData2 = IdGenerator.getId()

		val sourceData1 = IdGenerator.getId()
		val sourceData2 = IdGenerator.getId()
		val sourceData3 = IdGenerator.getId()
		val sourceData4 = IdGenerator.getId()

		// Set up the update target
		val targetRoot = Files.createTempDirectory(javaClass.simpleName)
		val targetFile1 = Files.createTempFile(targetRoot, javaClass.simpleName, "")
		val targetSubFolder = Files.createTempDirectory(targetRoot, javaClass.simpleName)
		val targetFile2 = Files.createTempFile(targetSubFolder, javaClass.simpleName, "")

		// Set up the update source
		val sourceRoot = Files.createTempDirectory(javaClass.simpleName)
		val sourceFile1 = sourceRoot.resolve(targetFile1.fileName)
		val sourceFile2 = Files.createTempFile(sourceRoot, javaClass.simpleName, "")
		val sourceSubFolder = sourceRoot.resolve(targetSubFolder.fileName)
		Files.createDirectories(sourceSubFolder)
		val sourceFile3 = sourceSubFolder.resolve(targetFile2.fileName)
		val sourceFile4 = Files.createTempFile(sourceSubFolder, javaClass.simpleName, "")

		val targetFile3 = targetRoot.resolve(sourceFile2.fileName)
		val targetFile4 = targetSubFolder.resolve(sourceFile4.fileName)

		// Store test data
		FileUtil.save(targetData1, targetFile1)
		FileUtil.save(targetData2, targetFile2)
		FileUtil.save(sourceData1, sourceFile1)
		FileUtil.save(sourceData2, sourceFile2)
		FileUtil.save(sourceData3, sourceFile3)
		FileUtil.save(sourceData4, sourceFile4)

		// Create the update package
		val sourceZip = Paths.get(sourceRoot.toString() + ".zip")

		try {
			// Create the update source
			FileUtil.zip(sourceRoot, sourceZip)

			// Create the update task
			val task = UpdateTask(listOf(sourceZip.toAbsolutePath().toString(), targetRoot.toAbsolutePath().toString()))

			// Verify the target values before executing the task
			assertThat(FileUtil.load(targetFile1), `is`(targetData1))
			assertThat(FileUtil.load(targetFile2), `is`(targetData2))
			assertThat(Files.exists(targetFile3), `is`(false))
			assertThat(Files.exists(targetFile4), `is`(false))

			// Execute the update task
			task.execute()

			// Verify the target values after executing the task
			assertThat(FileUtil.load(targetFile1), `is`(sourceData1))
			assertThat(FileUtil.load(targetFile2), `is`(sourceData3))
			assertThat(FileUtil.load(targetFile3), `is`(sourceData2))
			assertThat(FileUtil.load(targetFile4), `is`(sourceData4))
		} finally {
			FileUtil.delete(targetRoot)
			FileUtil.delete(sourceRoot)
			FileUtil.delete(sourceZip)
		}
	}

}
