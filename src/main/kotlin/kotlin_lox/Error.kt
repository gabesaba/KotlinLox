package kotlin_lox

abstract class LoxError : Exception()

object ParseError : LoxError()

class RuntimeError(val token: Token, override val message: String) : LoxError()
