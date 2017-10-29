package com.xeomar.annex

import java.io.BufferedInputStream
import java.io.IOException

object Program {

	@JvmStatic
	fun main(commands: Array<String>) {
		start(commands)
	}

	fun start(commands: Array<String>) {
		println("Parsing commands...")

		when {
			commands.contains("--stream") -> readCommandsFromStdIn()
		}
	}


	private fun readCommandsFromStdIn() {
		println("Reading from stdin...")
		try {
			BufferedInputStream(System.`in`).use { input -> readCommandsFromBytes(input.readAllBytes()) }
		} catch (e: IOException) {
			e.printStackTrace()
		}
	}

	private fun readCommandsFromBytes(bytes: ByteArray) {

	}

}
