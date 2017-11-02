package com.xeomar.annex

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class UpdateTask(parameters: List<String>) : AnnexTask(parameters) {

	override fun needsElevation(): Boolean {
		val target = Paths.get(getParameters()[1])
		return Files.exists(target) && !Files.isWritable(target)
	}


	override fun execute() {
		val source = File(getParameters()[0])
		val target = File(getParameters()[1])


	}


}
