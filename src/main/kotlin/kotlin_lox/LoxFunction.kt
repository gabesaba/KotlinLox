package kotlin_lox

class LoxFunction(private val declaration: Stmt.Function, private val closure: Environment) :
    LoxCallable {
  override fun arity(): Int {
    return declaration.params.size
  }

  override fun call(interpreter: Interpreter, arguments: List<Expr>): LoxObject {
    val environment = Environment(closure)
    for ((param, arg) in declaration.params.zip(arguments)) {
      environment.define(param.identifier, interpreter.evaluate(arg))
    }
    try {
      interpreter.executeBlock(declaration.body, environment)
    } catch (returnValue: Stmt.Return.ReturnValue) {
      return returnValue.value
    }
    return LoxNil
  }

  override fun toString() = "<fn ${declaration.name}>"
}
