package kotlin_lox

interface Stmt {
  interface Visitor {
    fun visit(print: Print)
    fun visit(expression: Expression)
    fun visit(v: Var)
  }

  fun accept(visitor: Visitor)

  class Print(val expr: Expr) : Stmt {
    override fun accept(visitor: Stmt.Visitor) {
      visitor.visit(this)
    }
  }

  class Expression(val expr: Expr) : Stmt {
    override fun accept(visitor: Stmt.Visitor) {
      visitor.visit(this)
    }
  }

  class Var(val identifier: String, val expr: Expr) : Stmt {
    override fun accept(visitor: Stmt.Visitor) {
      visitor.visit(this)
    }
  }
}

interface Expr {
  interface Visitor {
    fun visit(expr: Literal): Literal

    fun visit(unary: Unary): Literal

    fun visit(binary: Binary): Literal

    fun visit(grouping: Grouping): Literal

    fun visit(variable: Variable): Literal
  }

  fun accept(visitor: Visitor): Literal
}

sealed class Literal : Expr {
  override fun accept(visitor: Expr.Visitor): Literal {
    return visitor.visit(this)
  }
}

data class LoxNumber(val value: Double) : Literal() {
  override fun toString(): String {
    return value.toString()
  }
}

data class LoxString(val value: String) : Literal() {
  override fun toString(): String {
    return value
  }
}

data class LoxBoolean(val value: Boolean) : Literal() {
  override fun toString(): String {
    return value.toString()
  }
}

object LoxNil : Literal() {
  override fun toString() = "nil"
}

fun parsePrimary(token: Token): Literal {
  return when (token.type) {
    TokenType.NUMBER -> LoxNumber(token.lexeme.toDouble())
    TokenType.STRING -> LoxString(token.lexeme.substring(1 until token.lexeme.length - 1))
    TokenType.TRUE -> LoxBoolean(true)
    TokenType.FALSE -> LoxBoolean(false)
    TokenType.NIL -> LoxNil
    else -> LoxNil
  }
}

data class Unary(val operator: Token, val operand: Expr) : Expr {
  override fun accept(visitor: Expr.Visitor): Literal {
    return visitor.visit(this)
  }
}

data class Binary(val operator: Token, val left: Expr, val right: Expr) : Expr {
  override fun accept(visitor: Expr.Visitor): Literal {
    return visitor.visit(this)
  }
}

data class Grouping(val expr: Expr) : Expr {
  override fun accept(visitor: Expr.Visitor): Literal {
    return visitor.visit(this)
  }
}

data class Variable(val token: Token) : Expr {
  override fun accept(visitor: Expr.Visitor): Literal {
    return visitor.visit(this)
  }

  val identifier = token.lexeme
}
