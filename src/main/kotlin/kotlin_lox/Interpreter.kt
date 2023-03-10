package kotlin_lox

class Interpreter(private var env: Environment = Environment()) : Expr.Visitor, Stmt.Visitor {
  init {
    env.define("clock", Clock)
  }

  sealed interface InterpretResult {
    object Success : InterpretResult
    data class Failure(val error: RuntimeError) : InterpretResult
  }
  fun interpret(statements: List<Stmt>): InterpretResult {
    for (statement in statements) {
      try {
        execute(statement)
      } catch (error: RuntimeError) {
        runtimeError(error)
        return InterpretResult.Failure(error)
      }
    }
    return InterpretResult.Success
  }

  private fun execute(stmt: Stmt) {
    stmt.accept(this)
  }

  fun evaluate(expr: Expr): LoxObject {
    return expr.accept(this)
  }

  override fun visit(expr: Literal): Literal {
    return expr
  }

  override fun visit(unary: Unary): Literal {
    return when (unary) {
      is Unary.Negate -> {
        val operand = unary.operand.accept(this)
        if (operand !is LoxNumber) {
          throw RuntimeError(unary, "Expected number.")
        }
        LoxNumber(-operand.value)
      }
      is Unary.Not -> {
        val operand = unary.operand.accept(this)
        if (operand !is LoxBoolean) {
          throw RuntimeError(unary, "Expected boolean.")
        }
        LoxBoolean(!operand.value)
      }
      is Unary.Increment, is Unary.Decrement, is Unary.PostfixIncrement, is Unary.PostfixDecrement -> {
        if (unary.operand !is Variable) {
          throw RuntimeError(unary, "Operand must be a variable.")
        }
        val operand = unary.operand.accept(this)
        if (operand !is LoxNumber) {
          throw RuntimeError(unary, "Expected number.")
        }
        when (unary) {
          is Unary.Increment, is Unary.PostfixIncrement -> LoxNumber(operand.value + 1.0)
          is Unary.Decrement, is Unary.PostfixDecrement -> LoxNumber(operand.value - 1.0)
          else -> throw RuntimeError(unary, "Illegal state.")
        }
      }
    }
  }

  override fun visit(binary: Binary): Literal {
    val left = binary.left.accept(this)
    val right = binary.right.accept(this)

    val binaryDoubleFunction =
        when (binary.operator.type) {
          TokenType.PLUS -> {
            if (left is LoxString) {
              return convertString(String::plus)(left, LoxString(right.toString()))
            }
            if (left is LoxNumber && right is LoxNumber) {
              return convertDouble(Double::plus)(left, right)
            }
            throw RuntimeError(binary, "Expected two numbers or two strings.")
          }
          TokenType.MINUS -> convertDouble(Double::minus)
          TokenType.STAR -> convertDouble(Double::times)
          TokenType.SLASH -> convertDouble(Double::div)
          TokenType.GREATER -> convertDoublePredicate { a, b -> a > b }
          TokenType.GREATER_EQUAL -> convertDoublePredicate { a, b -> a >= b }
          TokenType.LESS -> convertDoublePredicate { a, b -> a < b }
          TokenType.LESS_EQUAL -> convertDoublePredicate { a, b -> a <= b }
          // Since we're using Kotlin data classes, we can rely on underlying equals implementation.
          TokenType.EQUAL_EQUAL -> return LoxBoolean(left == right)
          TokenType.BANG_EQUAL -> return LoxBoolean(left != right)
          else -> throw RuntimeError(binary, "Unexpected binary operator.")
        }

    if (left is LoxNumber && right is LoxNumber) {
      return binaryDoubleFunction(left, right)
    }
    throw RuntimeError(binary, "Expected two numbers.")
  }

  override fun visit(grouping: Grouping): LoxObject {
    return grouping.expr.accept(this)
  }

  override fun visit(variable: Variable): LoxObject {
    return env.get(variable.identifier)
        ?: throw RuntimeError(variable, "Cannot resolve identifier ${variable.identifier}")
  }

  override fun visit(assign: Expr.Assign): LoxObject {
    val assignValue = evaluate(assign.right)
    val returnValue = when (assign.right) {
      is Unary.PostfixDecrement, is Unary.PostfixIncrement ->  evaluate(assign.variable)
      else -> assignValue
    }
    val success = env.assign(assign.variable.identifier, assignValue)
    if (!success) {
      throw RuntimeError(assign.variable, "Undefined variable ${assign.variable.identifier}.")
    }
    return returnValue
  }

  override fun visit(logicalExpression: LogicalExpression): LoxObject {
    val left = evaluate(logicalExpression.left)
    when (Pair(left, logicalExpression.type.type)) {
      Pair(LoxBoolean(true), TokenType.OR) -> return left
      Pair(LoxBoolean(false), TokenType.OR) -> return evaluate(logicalExpression.right)
      Pair(LoxBoolean(true), TokenType.AND) -> return evaluate(logicalExpression.right)
      Pair(LoxBoolean(false), TokenType.AND) -> return left
      else -> throw RuntimeError(logicalExpression, "Expected boolean on LHS of and/or.")
    }
  }

  override fun visit(call: Call): LoxObject {
    val callable = evaluate(call.callable)
    if (callable !is LoxCallable) {
      throw RuntimeError(call, "Can only call functions and classes.")
    }

    if (call.args.size != callable.arity()) {
      throw RuntimeError(call, "Expected ${callable.arity()} arguments but got ${call.args.size}.")
    }
    return callable.call(this, call.args)
  }

  override fun visit(print: Stmt.Print) {
    println(evaluate(print.expr))
  }

  override fun visit(expression: Stmt.Expression) {
    evaluate(expression.expr)
  }

  override fun visit(v: Stmt.Var) {
    env = env.split()
    env.define(v.variable.identifier)
    if (v.expr != null) {
      env.assign(v.variable.identifier, evaluate(v.expr))
    }
  }

  override fun visit(block: Stmt.Block) {
    executeBlock(block)
  }

  fun executeBlock(block: Stmt.Block, executionEnvironment: Environment = Environment(env)) {
    val oldEnv = env
    env = executionEnvironment
    try {
      for (statement in block.statements) {
        execute(statement)
      }
    } finally {
      env = oldEnv
    }
  }

  override fun visit(ifStmt: Stmt.If) {
    val truthinessFunction = makeCheckTruthiness(ifStmt)
    if (truthinessFunction(evaluate(ifStmt.condition))) {
      execute(ifStmt.thenBranch)
    } else {
      execute(ifStmt.elseBranch)
    }
  }

  override fun visit(whileStmt: Stmt.While) {
    val truthinessFunction = makeCheckTruthiness(whileStmt)
    while (truthinessFunction(evaluate(whileStmt.condition))) {
      execute(whileStmt.block)
    }
  }

  override fun visit(returnStmt: Stmt.Return) {
    throw Stmt.Return.ReturnValue(evaluate(returnStmt.value))
  }

  override fun visit(function: Stmt.Function) {
    env.define(function.name, LoxFunction(function, env))
  }

  private fun makeCheckTruthiness(debuggable: Debuggable): (LoxObject) -> Boolean {
    fun checkTruthiness(literal: LoxObject): Boolean {
      return when (literal) {
        LoxBoolean(true) -> true
        LoxBoolean(false) -> false
        else -> throw RuntimeError(debuggable, "Expect boolean in condition.")
      }
    }
    return ::checkTruthiness
  }
}

// Convert function of (Double, Double) -> Double to (LoxNumber, LoxNumber) -> LoxNumber
private fun convertDouble(f: (Double, Double) -> Double): (LoxNumber, LoxNumber) -> LoxNumber {
  return { a, b -> LoxNumber(f(a.value, b.value)) }
}

// Convert function of (String, String) -> String to (LoxString, LoxString) -> LoxString
private fun convertString(f: (String, String) -> String): (LoxString, LoxString) -> LoxString {
  return { a, b -> LoxString(f(a.value, b.value)) }
}

// Convert function of (Double, Double) -> Boolean to (LoxNumber, LoxNumber) -> LoxBoolean
private fun convertDoublePredicate(
    f: (Double, Double) -> Boolean
): (LoxNumber, LoxNumber) -> LoxBoolean {
  return { a, b -> LoxBoolean(f(a.value, b.value)) }
}
