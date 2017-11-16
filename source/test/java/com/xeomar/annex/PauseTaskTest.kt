package com.xeomar.annex

import org.hamcrest.Matchers
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class PauseTaskTest {

	@Test
	fun testExecute() {
		val delay = 50L
		val start = System.currentTimeMillis()
		val result = TaskResult.parse(Program().runTasksFromString("${PauseTask.command} $delay"))
		val stop = System.currentTimeMillis()

		assertThat(stop - start, `is`(Matchers.greaterThanOrEqualTo(delay)))
		assertThat(result.code, `is`(0))
		assertThat(result.message, `is`("success"))
	}

	@Test
	fun testExecuteFailure() {
		val result = TaskResult.parse(Program().runTasksFromString("${PauseTask.command} forever"))

		assertThat(result.code, `is`(1))
		assertThat(result.message, `is`("NumberFormatException: For input string: \"forever\""))
	}

}
