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
            commands[0]=="--stream" -> readTasksFromStdIn()
            commands[0]=="--file" -> readTasksFromFile(File(commands[1]))
        }
    }

    fun readTasksFromStdIn(): List<AnnexTask> {
        return readTasksFromStream(System.`in`)
    }

    fun readTasksFromFile( file: File ): List<AnnexTask> {
        return readTasksFromStream( FileInputStream( file ));
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
