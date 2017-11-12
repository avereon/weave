package com.xeomar.annex

class LaunchTask(parameters: List<String>) : AnnexTask(command, parameters) {

	override fun execute(): TaskResult {
		val builder = ProcessBuilder()
		builder.command().addAll(parameters)
		val process = builder.start()

		// TODO Without waiting for the process to finish, this task is asynchronous

		return TaskResult(TaskStatus.SUCCESS, "success")
	}

	companion object {
		val command = "launch"
	}

}
