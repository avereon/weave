package com.xeomar.annex

abstract class AnnexTask(private val parameters: List<String>) {

	abstract fun execute()

	open fun needsElevation(): Boolean {
		return false
	}

	fun getParameters(): List<String> {
		return parameters
	}

}
