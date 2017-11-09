package com.xeomar.annex

class UnpackTask(command: String, parameters: List<String>) : AnnexTask(command, parameters) {

	override fun execute(): TaskResult {
		return TaskResult(TaskStatus.SUCCESS, "success")
	}

}
