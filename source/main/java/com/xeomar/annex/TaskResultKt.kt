package com.xeomar.annex

class TaskResultKt(val code: Int, val message: String = "") {

	override fun toString(): String {
		return code.toString() + " " + message
	}

	companion object Factory {
		fun parse(string: String): TaskResultKt {
			val index = string.indexOf(' ')
			return if( index < 0 ) {
				TaskResultKt(Integer.parseInt(string))
			} else {
				TaskResultKt(Integer.parseInt(string.substring(0,index)), string.substring(index+1))
			}
		}
	}

}
