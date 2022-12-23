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

fun run(source: String) {
  val scanner = Scanner(source)
  val tokens = scanner.scanTokens()
  val parser = Parser(tokens)

  when (val parseResult = parser.parse()) {
    is Success -> println(parseResult.parseTree)
    is Failure -> println("Parse failed.")
  }
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

fun error(token: Token, message: String) {
  if (token.type == TokenType.EOF) {
    report(token.line, " at end", message)
  } else {
    report(token.line, " at ${token.lexeme}", message)
  }
}

private fun report(line: Int, where: String, message: String) {
  hadError = true
  println("[line $line] Error$where: $message")
}
