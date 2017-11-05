package com.xeomar.annex

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class UpdateTask(command: String, parameters: List<String>) : AnnexTask(command, parameters) {

	override fun needsElevation(): Boolean {
		val target = Paths.get(getParameters()[1])
		return Files.exists(target) && !Files.isWritable(target)
	}


	override fun execute(): String {
		val source = File(getParameters()[0])
		val target = File(getParameters()[1])

		return "success"
	}


}
