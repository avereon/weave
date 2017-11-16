package com.xeomar.annex

import org.hamcrest.Matchers
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class LaunchTaskTest {

	@Test
	fun testExecute() {
		val result = TaskResult.parse(Program().runTasksFromString("launch java"))
		assertThat(result.code, `is`(0))
		assertThat(result.message, Matchers.`is`("java"))
	}

	@Test
	fun testExecuteFailure() {
		val result = TaskResult.parse(Program().runTasksFromString("launch invalid"))
		assertThat(result.code, `is`(1))
		assertThat(result.message, Matchers.startsWith("IOException: Cannot run program \"invalid\""))
	}

}