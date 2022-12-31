package kotlin_lox

abstract class LoxError : Exception()

object ParseError : LoxError()

class RuntimeError(debuggable: Debuggable, override val message: String) : LoxError() {
  val debugInfo = debuggable.debugInfo
}

enum class ResolverError {
  VariableDefinedTwice,
  VariableReadInInitializer,
  VariableNeverSet,
  VariableNeverRead,
  VariableUndefined,
  ReturnOutsideOfFunction
}

class DebugInfo(token: Token) {
  val line = token.line
  val atEnd = token.type == TokenType.EOF
  val lexeme = token.lexeme
}

interface Debuggable {
  val debugInfo: DebugInfo
}
