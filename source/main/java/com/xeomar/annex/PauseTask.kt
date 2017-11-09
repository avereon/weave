package com.xeomar.annex

class PauseTask(command: String, parameters: List<String>) : AnnexTask(command, parameters) {

	override fun execute(): TaskResult {
		Thread.sleep(getParameters()[0].toLong())
		return TaskResult(200, "success")
	}

}
