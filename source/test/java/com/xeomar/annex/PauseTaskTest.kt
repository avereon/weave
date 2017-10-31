package com.xeomar.annex

import org.hamcrest.Matchers
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import java.util.*

class PauseTaskTest {

	@Test
	fun testExecute() {
		val delay = 1000L
		val parameters = Arrays.asList(delay.toString())
		val task = PauseTask(parameters)

		val start = System.currentTimeMillis()
		task.execute()
		val stop = System.currentTimeMillis()
		assertThat(stop - start, `is`(Matchers.greaterThanOrEqualTo(delay)))
	}

}
