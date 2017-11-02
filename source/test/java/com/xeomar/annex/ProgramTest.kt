package com.xeomar.annex

import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.io.StringWriter

class ProgramTest {

	@Test
	@Throws(Exception::class)
	fun testReadCommandsFromStdin() {
		val originalInput = System.`in`
		val originalOutput = System.out
		val outputBuffer = ByteArrayOutputStream()
		val input = ByteArrayInputStream("update source target\nupdate source2 target2".toByteArray(charset("utf-8")))
		val output = PrintStream(outputBuffer)

		System.setIn(input)
		System.setOut(output)
		Program.runTasksFromStdIn()
		System.setOut(originalOutput)
		System.setIn(originalInput)

		// TODO Read the results from System.out

		assertThat(outputBuffer.toString("utf-8"), `is`("com.xeomar.annex.UpdateTask\ncom.xeomar.annex.UpdateTask\n"))

//		assertThat(tasks[0].getParameters()[0], `is`("source"))
//		assertThat(tasks[0].getParameters()[1], `is`("target"))
//		assertThat(tasks[1].getParameters()[0], `is`("source2"))
//		assertThat(tasks[1].getParameters()[1], `is`("target2"))
//		assertThat(tasks[0].getParameters().size, `is`(2))
//		assertThat(tasks[1].getParameters().size, `is`(2))
//		assertThat(tasks.size, `is`(2))
	}

	@Test
	@Throws(Exception::class)
	fun testReadCommandsFromBytes() {
		val input = ByteArrayInputStream("update source target\nupdate source2 target2".toByteArray(charset("utf-8")))
		val output = StringWriter()

		val tasks = Program.runTasksFromStream(input, output)

//		assertThat(tasks[0].getParameters()[0], `is`("source"))
//		assertThat(tasks[0].getParameters()[1], `is`("target"))
//		assertThat(tasks[1].getParameters()[0], `is`("source2"))
//		assertThat(tasks[1].getParameters()[1], `is`("target2"))
//		assertThat(tasks[0].getParameters().size, `is`(2))
//		assertThat(tasks[1].getParameters().size, `is`(2))
//		assertThat(tasks.size, `is`(2))
	}

}
