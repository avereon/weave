package com.xeomar.annex

class LaunchTask(command: String, parameters: List<String>) : AnnexTask(command, parameters) {

	override fun execute(): String {
		val builder = ProcessBuilder()
		builder.command().addAll(getParameters())
		val process = builder.start()

		return "success"
	}

}
