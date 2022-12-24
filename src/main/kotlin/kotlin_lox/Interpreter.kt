package kotlin_lox

class Interpreter : Expr.Visitor, Stmt.Visitor {
  fun interpret(statements: List<Stmt>) {
    for (statement in statements) {
      try {
        execute(statement)
      } catch (error: RuntimeError) {
        runtimeError(error)
      }
    }
  }

  fun interpret(expr: Expr): Literal? {
    return try {
      evaluate(expr)
    } catch (error: RuntimeError) {
      runtimeError(error)
      null
    }
  }

  private fun execute(stmt: Stmt) {
    stmt.accept(this)
  }

  private fun evaluate(expr: Expr): Literal {
    return expr.accept(this)
  }

  override fun visit(expr: Literal): Literal {
    return expr
  }

  override fun visit(unary: Unary): Literal {
    val operand = unary.operand.accept(this)
    when (unary.operator.type) {
      TokenType.MINUS -> {
        if (operand !is LoxNumber) {
          throw RuntimeError(unary.operator, "Expected number.")
        }
        return LoxNumber(-operand.value)
      }
      TokenType.BANG -> {
        if (operand !is LoxBoolean) {
          throw RuntimeError(unary.operator, "Expected boolean.")
        }
        return LoxBoolean(!operand.value)
      }
      else -> throw RuntimeError(unary.operator, "Unexpected unary operator.")
    }
  }

  override fun visit(binary: Binary): Literal {
    val left = binary.left.accept(this)
    val right = binary.right.accept(this)

    val binaryDoubleFunction =
        when (binary.operator.type) {
          TokenType.PLUS -> {
            if (left is LoxString && right is LoxString) {
              return convertString(String::plus)(left, right)
            }
            if (left is LoxNumber && right is LoxNumber) {
              return convertDouble(Double::plus)(left, right)
            }
            throw RuntimeError(binary.operator, "Expected two numbers or two strings.")
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
          else -> throw RuntimeError(binary.operator, "Unexpected binary operator.")
        }

    if (left is LoxNumber && right is LoxNumber) {
      return binaryDoubleFunction(left, right)
    }
    throw RuntimeError(binary.operator, "Expected two numbers.")
  }

  override fun visit(grouping: Grouping): Literal {
    return grouping.expr.accept(this)
  }

  override fun visit(stmt: Print) {
    println(evaluate(stmt.expr))
  }

  override fun visit(stmt: Expression) {
    evaluate(stmt.expr)
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
