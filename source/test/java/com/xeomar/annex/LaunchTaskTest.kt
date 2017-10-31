package com.xeomar.annex

import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import java.util.*

class LaunchTaskTest {

	@Test
	fun testExecute() {
		val program = "xenon"
		val task = LaunchTask(Arrays.asList(program))
		assertThat( task.getParameters()[0], `is`( program ))
		assertThat( task.getParameters().size, `is`(1))
	}

}