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
		val task = LaunchTask( Arrays.asList(program, file))
		assertThat(task.parameters[0], `is`(program))
		assertThat(task.parameters[1], `is`(file))
		assertThat(task.parameters.size, `is`(2))
	}

}