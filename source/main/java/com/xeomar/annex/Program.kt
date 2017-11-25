package com.xeomar.annex

import com.xeomar.product.Product
import com.xeomar.product.ProductBundle
import com.xeomar.product.ProductCard
import com.xeomar.util.OperatingSystem
import org.slf4j.LoggerFactory
import java.io.*
import java.nio.charset.Charset
import java.nio.file.Path

class Program : Product {

	private val log = LoggerFactory.getLogger(Program::class.java)

	private val card = ProductCard()

	private val resourceBundle = ProductBundle(javaClass.classLoader)

	private val programDataFolder = OperatingSystem.getUserProgramDataFolder(card.artifact, card.name)

	private var title = card.name

	private var elevatedProcess: Process? = null

	override fun getCard(): ProductCard = card

	override fun getClassLoader(): ClassLoader = javaClass.classLoader

	override fun getResourceBundle(): ProductBundle = resourceBundle

	override fun getDataFolder(): Path = programDataFolder

	fun run(commands: Array<String>) {
		printHeader(card)

		log.debug("Parsing commands...")

		for ((index, command) in commands.withIndex()) {
			when (command) {
				"--title" -> title = commands[index + 1]
				"--stream" -> runTasksFromStdIn()
				"--file" -> runTasksFromFile(File(commands[1]))
			}
		}
	}

	private fun runTasksFromStdIn() {
		return runTasksFromStream(System.`in`, System.out)
	}

	private fun runTasksFromFile(file: File) {
		return runTasksFromStream(FileInputStream(file), ByteArrayOutputStream())
	}

	private fun runTasksFromStream(input: InputStream, output: OutputStream) {
		runTasksFromReader(InputStreamReader(input, "utf-8"), OutputStreamWriter(output, "utf-8"))
	}

	fun runTasksFromString(commands: String): String {
		val reader = StringReader(commands)
		val writer = StringWriter()
		runTasksFromReader(reader, writer)
		return writer.toString().trim()
	}

	fun runTasksFromReader(reader: Reader, writer: Writer) {
		val buffer = BufferedReader(reader)
		val printWriter = PrintWriter(writer)

		var line = buffer.readLine()
		while (line != null) {
			val result = executeTask(parseTask(line))
			printWriter.print(result)
			//printWriter.print("\r\n")
			printWriter.print("\n")
			printWriter.flush()

			line = buffer.readLine()
		}
	}

	private fun executeTask(task: AnnexTask): TaskResult {
		// Now for the hard part, figuring out how to execute the tasks.
		// The reason this is hard is because some of the update commands will
		// require elevated privileges. But we don't want to execute programs
		// with an elevated process. If this process is elevated, we need to take
		// care not to become a security issue by allowing others to execute
		// elevated processes through the program.
		//
		// That means that executing the tasks in sequence may be challenging if
		// there are some that need to be elevated and some that should not. If
		// the tasks all need to be elevated, or all do not need to be elevated,
		// the task execution is pretty straight forward. If they need to be mixed
		// then it is not trivial.
		//
		// If all the tasks do not need elevation then all the tasks can be
		// executed in this process, otherwise an elevated process will need to be
		// started and some tasks executed on the elevated process. This will also
		// cover the situation where all tasks need to be elevated.

		// If needsElevation is true then a separate, elevated, process will need
		// to be started to execute some tasks.
		return try {
			if (task.needsElevation()) {
				if (elevatedProcess == null) {
					// TODO Create elevated updater
					val processBuilder = ProcessBuilder()
					elevatedProcess = OperatingSystem.startProcessElevated(title, processBuilder)
				}
				elevatedProcess?.outputStream?.writer(Charset.forName("utf-8"))?.write(task.execute().toString())
				TaskResult.parse(BufferedReader(InputStreamReader(elevatedProcess?.inputStream)).readLine())
			} else {
				task.execute()
			}
		} catch (exception: Exception) {
			TaskResult(TaskStatus.FAILURE, "${exception.javaClass.simpleName}: ${exception.message!!}")
		}
	}

	fun parseTask(line: String): AnnexTask {
		var parameters = line.split(" ")
		val command = parameters[0]
		parameters = parameters.subList(1, parameters.size)
		return when (command) {
			LaunchTask.command -> LaunchTask(parameters)
			PauseTask.command -> PauseTask(parameters)
			UpdateTask.command -> UpdateTask(parameters)
			else -> throw IllegalArgumentException("Unknown command: " + command)
		}
	}

	private fun printHeader(card: ProductCard) {
		System.err.println(card.name + " " + card.version)
		System.err.println("Java " + System.getProperty("java.runtime.version"))
	}

	companion object {
		@JvmStatic
		fun main(commands: Array<String>) {
			Program().run(commands)
		}
	}

}
