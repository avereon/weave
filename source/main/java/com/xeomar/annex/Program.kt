package com.xeomar.annex

import java.io.*
import java.util.*

object Program {

	@JvmStatic
	fun main(commands: Array<String>) {
		run(commands)
	}

	fun run(commands: Array<String>) {
		println("Parsing commands...")

		val tasks = when {
			commands[0] == "--stream" -> readTasksFromStdIn()
			commands[0] == "--file" -> readTasksFromFile(File(commands[1]))
			else -> ArrayList()
		}

		for (task in tasks) {
			task.execute()
		}
	}

	fun readTasksFromStdIn(): List<AnnexTask> {
		return readTasksFromStream(System.`in`)
	}

	fun readTasksFromFile(file: File): List<AnnexTask> {
		return readTasksFromStream(FileInputStream(file));
	}

	fun readTasksFromStream(stream: InputStream): List<AnnexTask> {
		val tasks = ArrayList<AnnexTask>()
		BufferedReader(InputStreamReader(stream)).lines().forEach { line -> tasks.add(parseTask(line)) }
		return tasks
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
