package com.xeomar.annex

class PauseTaskKt(parameters: List<String>) : AnnexTaskKt(command, parameters) {

	override fun execute(): TaskResultKt {
		Thread.sleep(parameters[0].toLong())
		return TaskResultKt(TaskStatusKt.SUCCESS, "success")
	}

	companion object {
		val command = UpdateFlag.PAUSE
	}

}
