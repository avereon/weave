package com.xeomar.annex

abstract class AnnexTask(val command: String,  val parameters: List<String>) {

	abstract fun execute():TaskResult

	open fun needsElevation(): Boolean {
		return false
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
