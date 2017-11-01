package com.xeomar.annex

import org.junit.Test

import java.io.ByteArrayInputStream

import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat

class ProgramTest {

	@Test
	@Throws(Exception::class)
	fun testReadCommandsFromStdin() {
		val input = ByteArrayInputStream("update source target\nupdate source2 target2".toByteArray(charset("utf-8")))
		System.setIn(input)
		val tasks = Program.readTasksFromStdIn()

		assertThat(tasks[0].getParameters()[0], `is`("source"))
		assertThat(tasks[0].getParameters()[1], `is`("target"))
		assertThat(tasks[1].getParameters()[0], `is`("source2"))
		assertThat(tasks[1].getParameters()[1], `is`("target2"))
		assertThat(tasks[0].getParameters().size, `is`(2))
		assertThat(tasks[1].getParameters().size, `is`(2))
		assertThat(tasks.size, `is`(2))
	}

	@Test
	@Throws(Exception::class)
	fun testReadCommandsFromBytes() {
		val input = ByteArrayInputStream("update source target\nupdate source2 target2".toByteArray(charset("utf-8")))
		val tasks = Program.readTasksFromStream(input)

		assertThat(tasks[0].getParameters()[0], `is`("source"))
		assertThat(tasks[0].getParameters()[1], `is`("target"))
		assertThat(tasks[1].getParameters()[0], `is`("source2"))
		assertThat(tasks[1].getParameters()[1], `is`("target2"))
		assertThat(tasks[0].getParameters().size, `is`(2))
		assertThat(tasks[1].getParameters().size, `is`(2))
		assertThat(tasks.size, `is`(2))
	}

}
