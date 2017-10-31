package com.xeomar.annex

class PauseTask(parameters: List<String>) : AnnexTask(parameters) {

	override fun execute() {
		Thread.sleep(getParameters()[0].toLong())
	}

}
