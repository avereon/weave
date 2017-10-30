package com.xeomar.annex

import java.io.*
import java.util.*

object Program {

	@JvmStatic
	fun main(commands: Array<String>) {
		start(commands)
	}

	fun start(commands: Array<String>) {
		println("Parsing commands...")

		when {
			commands.contains("--stream") -> readCommandsFromStdIn()
		}
	}

	private fun readCommandsFromStdIn() {
		println("Reading from stdin...")
		try {
			readTasksFromStream(System.`in`)
		} catch (e: IOException) {
			e.printStackTrace()
		}
	}

	fun readTasksFromBytes(bytes: ByteArray): List<AnnexTask> {
		return readTasksFromStream(ByteArrayInputStream(bytes))
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
			"update" -> UpdateTask(parameters)
			"unpack" -> UnpackTask(parameters)
			else -> throw IllegalArgumentException("Unknown command: " + command)
		}
	}

}
