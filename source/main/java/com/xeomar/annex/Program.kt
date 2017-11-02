package com.xeomar.annex

import java.io.*

object Program {

	@JvmStatic
	fun main(commands: Array<String>) {
		run(commands)
	}

	fun run(commands: Array<String>) {
		println("Parsing commands...")

		when {
			commands[0] == "--stream" -> runTasksFromStdIn()
			commands[0] == "--file" -> runTasksFromFile(File(commands[1]))
		}
	}

	fun runTasksFromStdIn() {
		return runTasksFromStream(System.`in`, OutputStreamWriter(System.out, "utf-8" ))
	}

	fun runTasksFromFile(file: File) {
		return runTasksFromStream(FileInputStream(file), StringWriter())
	}

	fun runTasksFromStream(stream: InputStream, writer: Writer) {
		val printWriter = PrintWriter( writer )
		BufferedReader(InputStreamReader(stream)).lines().forEach { line ->
			printWriter.println( executeTask( parseTask(line) ) )
			printWriter.flush()
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

		var needsElevation = task.needsElevation()

		// If needsElevation is true then a separate, elevated, process will need
		// to be started to execute some tasks.

		return task.javaClass.name
	}

	fun parseTask(line: String): AnnexTask {
		var parameters = line.split(" ")
		val command = parameters[0]
		parameters = parameters.subList(1, parameters.size)
		return when (command) {
			"launch" -> LaunchTask(parameters)
			"pause" -> PauseTask(parameters)
			"unpack" -> UnpackTask(parameters)
			"update" -> UpdateTask(parameters)
			else -> throw IllegalArgumentException("Unknown command: " + command)
		}
	}

}
