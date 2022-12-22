package kotlin_lox

import kotlin.io.path.Path
import kotlin.io.path.notExists
import kotlin.io.path.readText
import kotlin.system.exitProcess

private var hadError = false

fun main(args: Array<String>) {
  when (args.size) {
    0 -> runPrompt()
    1 -> interpretFile(args[0])
    else -> {
      println("Usage: klox [script]")
      exitProcess(64)
    }
  }
}

fun interpretFile(fileName: String) {
  val file = Path(fileName)
  if (file.notExists()) {
    println("File doesn't exist...")
    exitProcess(1)
  }
  run(file.readText())

  if (hadError) {
    exitProcess(65)
  }
}

fun run(source: String): String {

  val scanner = Scanner(source)
  val tokens = scanner.scanTokens()

  for (token in tokens) {
    println(token)
  }
  return source
}

fun runPrompt() {
  while (true) {
    print("-> ")
    val line = readlnOrNull() ?: break
    run(line)
    hadError = false
  }
}

fun error(line: Int, message: String) {
  report(line, "", message)
}

private fun report(line: Int, where: String, message: String) {
  hadError = true
  println("[line $line] Error$where: $message")
}
