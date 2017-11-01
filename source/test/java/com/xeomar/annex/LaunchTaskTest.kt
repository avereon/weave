package com.xeomar.annex

import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import java.util.*

class LaunchTaskTest {

	@Test
	fun testExecute() {
		val program = "xenon"
		val file = "test.txt"
		val task = LaunchTask(Arrays.asList(program, file))
		assertThat(task.getParameters()[0], `is`(program))
		assertThat(task.getParameters()[1], `is`(file))
		assertThat(task.getParameters().size, `is`(2))
	}

}