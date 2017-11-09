package com.xeomar.annex

class TaskResult( val code: Int,  val message: String = "") {

	override fun toString(): String {
		return code.toString() + " " + message
	}

	companion object Factory {
		fun parse(string: String): TaskResult {
			val index = string.indexOf(' ')
			return if( index < 0 ) {
				TaskResult(Integer.parseInt(string))
			} else {
				TaskResult(Integer.parseInt(string.substring(0,index)), string.substring(index+1))
			}
		}
	}

}
