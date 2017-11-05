package com.xeomar.annex

class UnpackTask(command: String, parameters: List<String>) : AnnexTask(command, parameters) {

	override fun execute(): String {
		return "success"
	}

}
