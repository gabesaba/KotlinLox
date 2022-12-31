package kotlin_lox

class TestOutput : LoxCallable {
  var output: LoxObject = LoxNil
  override fun arity(): Int {
    return 1
  }

  override fun call(interpreter: Interpreter, arguments: List<Expr>): LoxObject {
    output = interpreter.evaluate(arguments[0])
    return LoxNil
  }
}
