package com.xeomar.annex

import com.xeomar.razor.OperatingSystem
import org.slf4j.LoggerFactory
import java.io.*
import java.nio.charset.Charset

object Program {

	private val log = LoggerFactory.getLogger(Program.javaClass)

	private var title = "Annex"

	private var elevatedProcess: Process? = null

	@JvmStatic
	fun main(commands: Array<String>) {
		run(commands)
	}

	fun run(commands: Array<String>) {
		log.debug("Parsing commands...")

		for ((index, command) in commands.withIndex()) {
			when (command) {
				"--title" -> title = commands[index + 1]
				"--stream" -> runTasksFromStdIn()
				"--file" -> runTasksFromFile(File(commands[1]))
			}
		}
	}

	private fun runTasksFromStdIn() {
		return runTasksFromStream(System.`in`, System.out)
	}

	private fun runTasksFromFile(file: File) {
		return runTasksFromStream(FileInputStream(file), ByteArrayOutputStream())
	}

	private fun runTasksFromStream(input: InputStream, output: OutputStream) {
		runTasksFromReader(InputStreamReader(input, "utf-8"), OutputStreamWriter(output, "utf-8"))
	}

	fun runTasksFromReader(reader: Reader, writer: Writer) {
		val buffer = BufferedReader(reader)
		val printWriter = PrintWriter(writer)

		var line = buffer.readLine()
		while (line != null) {
			val result = executeTask(parseTask(line))
			printWriter.print(result)
			//printWriter.print("\r\n")
			printWriter.print("\n")
			printWriter.flush()

			line = buffer.readLine()
		}
	}

	private fun executeTask(task: AnnexTask): String {
		// NEXT Now for the hard part, figuring out how to execute the tasks
		// The reason this is hard is because some of the update commands will
		// require elevated privileges. But we don't want to execute programs
		// with an elevated process. If this process is elevated, we need to take
		// care not to become a security issue by allowing others to execute
		// elevated processes through the program.
		//
		// That means that executing the tasks in sequence may be challenging if
		// there are some that need to be elevated and some that should not. If
		// the tasks all need to be elevated, or all do not need to be elevated,
		// the task execution is pretty straight forward. If they need to be mixed
		// then it is not trivial.
		//
		// If all the tasks do not need elevation then all the tasks can be
		// executed in this process, otherwise an elevated process will need to be
		// started and some tasks executed on the elevated process. This will also
		// cover the situation where all tasks need to be elevated.

		// If needsElevation is true then a separate, elevated, process will need
		// to be started to execute some tasks.
		val needsElevation = task.needsElevation()
		if (needsElevation) {
			if (elevatedProcess == null) {
				// TODO Create elevated updater
				val processBuilder = ProcessBuilder()
				elevatedProcess = OperatingSystem.startProcessElevated(title, processBuilder)
			}

			elevatedProcess?.outputStream?.writer(Charset.forName("utf-8"))?.write(task.execute().toString())
			BufferedReader(InputStreamReader(elevatedProcess?.inputStream)).readLine()
		}

		return task.execute().toString()
	}

	fun parseTask(line: String): AnnexTask {
		var parameters = line.split(" ")
		val command = parameters[0]
		parameters = parameters.subList(1, parameters.size)
		return when (command) {
			"launch" -> LaunchTask("launch", parameters)
			"pause" -> PauseTask("pause", parameters)
			"unpack" -> UnpackTask("unpack", parameters)
			"update" -> UpdateTask("update", parameters)
			else -> throw IllegalArgumentException("Unknown command: " + command)
		}
	}

}
