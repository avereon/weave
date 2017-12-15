package com.xeomar.annex

class PauseTask(parameters: List<String>) : AnnexTask(command, parameters) {

	override fun execute(): TaskResult {
		Thread.sleep(parameters[0].toLong())
		return TaskResult(TaskStatus.SUCCESS, "success")
	}

	companion object {
		val command = UpdateFlag.PAUSE
	}

}
