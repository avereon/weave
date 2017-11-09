package com.xeomar.annex

import kotlinx.coroutines.experimental.launch
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import java.io.*
import java.nio.charset.Charset

class ProgramTest {

	@Test
	@Throws(Exception::class)
	fun testReadCommandsFromStdin() {
		val originalInput = System.`in`
		val originalOutput = System.out

		val outputPipe = PipedInputStream()
		val inputPipe = PipedOutputStream()

		System.setIn(PipedInputStream(inputPipe))
		System.setOut(PrintStream(PipedOutputStream(outputPipe)))
		try {
			launch {
				Program.run(arrayOf("--stream"))
			}

			inputPipe.write("update source target\n".toByteArray(charset("utf-8")))
			inputPipe.flush()
			val result1 = TaskResult.parse(outputPipe.bufferedReader(charset("utf-8")).readLine())
			assertThat(result1.code, `is`(TaskStatus.SUCCESS))
			assertThat(result1.message, `is`("success"))

			inputPipe.write("update source2 target2".toByteArray(charset("utf-8")))
			inputPipe.close()
			val result2 = TaskResult.parse(outputPipe.bufferedReader(charset("utf-8")).readLine())
			assertThat(result2.code, `is`(TaskStatus.SUCCESS))
			assertThat(result2.message, `is`("success"))
		} finally {
			// Restore the original streams
			System.setOut(originalOutput)
			System.setIn(originalInput)
		}
	}

	@Test
	@Throws(Exception::class)
	fun testReadCommandsFromBytes() {
		val outputPipe = PipedReader()
		val inputPipe = PipedWriter()

		val reader = PipedReader(inputPipe)
		val writer = PipedWriter(outputPipe)

		launch {
			Program.runTasksFromReader(reader, writer)
		}

		inputPipe.write("update source target\n")
		inputPipe.flush()
		val result1 = TaskResult.parse(outputPipe.buffered().readLine())
		assertThat(result1.code, `is`(TaskStatus.SUCCESS))
		assertThat(result1.message, `is`("success"))

		inputPipe.write("update source2 target2")
		inputPipe.close()
		val result2 = TaskResult.parse(outputPipe.buffered().readLine())
		assertThat(result2.code, `is`(TaskStatus.SUCCESS))
		assertThat(result2.message, `is`("success"))
	}

}
