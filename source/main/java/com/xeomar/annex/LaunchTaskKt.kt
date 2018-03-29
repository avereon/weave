package com.xeomar.annex

import com.xeomar.util.TextUtil

class LaunchTaskKt(parameters: List<String>) : AnnexTaskKt(command, parameters) {

	override fun execute(): TaskResultKt {
		val builder = ProcessBuilder()
		builder.command().addAll(parameters)
		val process = builder.start()

		// TODO Without waiting for the process to finish, this task is asynchronous

		val commands = TextUtil.toString(builder.command())
		return TaskResultKt(TaskStatusKt.SUCCESS, commands.substring(1, commands.length - 1))
	}

	companion object {
		val command = UpdateFlag.LAUNCH
	}

}
