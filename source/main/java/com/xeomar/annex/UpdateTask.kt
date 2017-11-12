package com.xeomar.annex

import com.xeomar.razor.FileUtil
import com.xeomar.razor.HashUtil
import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.zip.ZipException
import java.util.zip.ZipFile

class UpdateTask(command: String, parameters: List<String>) : AnnexTask(command, parameters) {

	private val log = LoggerFactory.getLogger(javaClass)

	private val DEL_SUFFIX = ".del"

	private val ADD_SUFFIX = ".add"

	override fun needsElevation(): Boolean {
		val target = Paths.get(getParameters()[1])
		return Files.exists(target) && !Files.isWritable(target)
	}

	override fun execute(): TaskResult {
		val source = Paths.get(getParameters()[0])
		val target = Paths.get(getParameters()[1])

		if (!Files.exists(source)) throw IllegalArgumentException("Source parameter not found: " + source)
		if (!Files.exists(target)) throw IllegalArgumentException("Target parameter not found: " + target)
		if (!Files.isDirectory(target)) throw IOException("Target must be a folder: " + target)


		log.trace("Staging: $source")

		try {
			stage(source, target)
		} catch (exception: ZipException) {
			throw IOException("Source not a valid zip file: " + source)
		} catch (throwable: Throwable) {
			log.warn("Update failed: $target", throwable.message)
			revert(target, target)
			throw throwable
		}


		log.trace("Committing: $target")
		commit(target, target)

		log.info("Updated: $target")


		return TaskResult(TaskStatus.SUCCESS, "success")
	}

	override fun toString(): String {
		return "Update " + getParameters()[1] + " ..."
	}

	private fun stage(source: Path, target: Path) {
		log.debug("Staging: $source.name to $target...")

		ZipFile(source.toFile()).use { zip ->
			val entries = zip.entries()
			while (entries.hasMoreElements()) {
				val entry = entries.nextElement()
				if (!stage(zip.getInputStream(entry), target, entry.name)) throw RuntimeException("Could not stage: " + target.resolve(entry.name))
			}
		}

		log.trace("Staged: $source.name to $target")
	}

	private fun stage(input: InputStream, target: Path, entry: String): Boolean {
		val file = target.resolve(entry)
		val folder = entry.endsWith("/")

		if (folder) {
			Files.createDirectories(file)
		} else {
			val delFile = file.parent.resolve(file.fileName.toString() + DEL_SUFFIX)
			val addFile = file.parent.resolve(file.fileName.toString() + ADD_SUFFIX)
			if (Files.exists(file)) Files.move(file, delFile, StandardCopyOption.ATOMIC_MOVE)
			Files.createDirectories(file.parent)
			FileOutputStream(addFile.toFile()).use { output -> IOUtils.copy(input, output) }
		}

		log.debug("Staging: $entry")
		return true
	}

	private fun revert(root: Path, target: Path) {
		// Revert staged changes.
		if (Files.isDirectory(target)) {
			for (file in Files.list(target)!!) revert(root, file)
		} else {
			if (target.fileName.toString().endsWith(DEL_SUFFIX)) {
				Files.move(target, FileUtil.removeExtension(target), StandardCopyOption.ATOMIC_MOVE)
			} else if (target.fileName.toString().endsWith(ADD_SUFFIX)) {
				Files.delete(target)
			}
		}
	}

	private fun commit(root: Path, target: Path) {
		// Commit staged changes.
		if (Files.isDirectory(target)) {
			for (file in Files.list(target)!!) commit(root, file)
		} else {
			if (target.fileName.toString().endsWith(ADD_SUFFIX)) {
				val sourceHash = HashUtil.hash(target)
				val file = FileUtil.removeExtension(target)
				Files.move(target, file)
				val targetHash = HashUtil.hash(file)
				if (targetHash != sourceHash)
					throw RuntimeException("Hash code mismatch commiting file: " + file)
				log.trace("Commit: " + root.relativize(file))
			} else if (target.fileName.toString().endsWith(DEL_SUFFIX)) {
				val file = removeSuffix(target, DEL_SUFFIX)
				if (!Files.exists(file)) log.trace("Remove: " + root.relativize(file))
				Files.delete(target)
			}
		}
	}

	private fun removeSuffix(path: Path, suffix: String): Path {
		val name = path.fileName.toString()
		val index = name.indexOf(suffix)
		return if (index < 0) path else path.parent.resolve(name.substring(0, index))
	}

}
