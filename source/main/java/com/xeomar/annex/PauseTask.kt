package com.xeomar.annex

class PauseTask(command: String, parameters: List<String>) : AnnexTask(command, parameters) {

	override fun execute(): String {
		Thread.sleep(getParameters()[0].toLong())
		return "success"
	}

}
