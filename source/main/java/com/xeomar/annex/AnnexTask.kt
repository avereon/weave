package com.xeomar.annex

abstract class AnnexTask(private val command: String, private val parameters: List<String>) {

	abstract fun execute():String

	open fun needsElevation(): Boolean {
		return false
	}

	fun getCommand(): String {
		return command
	}

	fun getParameters(): List<String> {
		return parameters
	}

	override fun toString(): String {
		val builder = StringBuilder()
		builder.append(command)
		for (parameter in parameters) {
			builder.append(" ")
			builder.append(parameter)
		}
		return builder.toString()
	}

}
