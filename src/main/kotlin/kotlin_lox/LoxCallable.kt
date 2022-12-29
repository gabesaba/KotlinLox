package kotlin_lox

interface LoxCallable : LoxObject {
  fun arity(): Int
  fun call(interpreter: Interpreter, arguments: List<Expr>): LoxObject
}
