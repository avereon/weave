package com.xeomar.annex

class LaunchTask(parameters: List<String>) : AnnexTask(parameters) {

	override fun execute() {
		val builder = ProcessBuilder()
		builder.command().addAll(getParameters())
		builder.start()
	}

}
