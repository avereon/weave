package com.xeomar.annex

class LaunchTask(command: String, parameters: List<String>) : AnnexTask(command, parameters) {

	override fun execute(): TaskResult {
		val builder = ProcessBuilder()
		builder.command().addAll(getParameters())
		val process = builder.start()

		// Without waiting for the process to finish, this task is asynchronous

		return TaskResult(TaskStatus.SUCCESS, "success")
	}

}
