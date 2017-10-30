package com.xeomar.annex

abstract class AnnexTask(private val parameters: List<String>) {

	abstract fun execute()

	fun getParameters(): List<String> {
		return parameters
	}

}
