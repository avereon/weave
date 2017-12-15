package com.xeomar.annex

import com.xeomar.util.TextUtil
import java.util.*

class LaunchTask(parameters: List<String>) : AnnexTask(command, parameters) {

	override fun execute(): TaskResult {
		val builder = ProcessBuilder()
		builder.command().addAll(parameters)
		val process = builder.start()

		// TODO Without waiting for the process to finish, this task is asynchronous

		val commands = TextUtil.toString(builder.command())
		return TaskResult(TaskStatus.SUCCESS, commands.substring(1, commands.length - 1))
	}

	companion object {
		val command = UpdateFlag.LAUNCH
	}

}
