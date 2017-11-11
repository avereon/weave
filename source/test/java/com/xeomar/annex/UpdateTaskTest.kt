package com.xeomar.annex

import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test
import java.util.*

class UpdateTaskTest {

	@Test
	fun testExecute() {
		val source = "source"
		val target = "target"
		val task = LaunchTask("update", Arrays.asList(source, target))
		Assert.assertThat(task.getParameters()[0], Matchers.`is`(source))
		Assert.assertThat(task.getParameters()[1], Matchers.`is`(target))
		Assert.assertThat(task.getParameters().size, Matchers.`is`(2))
	}

}
