package kotlin_lox

abstract class LoxError : Exception()

object ParseError : LoxError()
