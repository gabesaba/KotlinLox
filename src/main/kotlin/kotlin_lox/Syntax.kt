package kotlin_lox

interface Stmt {
  interface Visitor {
    fun visit(print: Print)
    fun visit(expression: Expression)
    fun visit(v: Var)
    fun visit(block: Block)
    fun visit(ifStmt: If)
    fun visit(whileStmt: While)
    fun visit(returnStmt: Return)
    fun visit(function: Function)
  }

  fun accept(visitor: Visitor)

  class Print(val expr: Expr) : Stmt {
    override fun accept(visitor: Visitor) {
      visitor.visit(this)
    }
  }

  class Expression(val expr: Expr) : Stmt {
    override fun accept(visitor: Visitor) {
      visitor.visit(this)
    }
  }

  class Var(val identifier: String, val expr: Expr) : Stmt {
    override fun accept(visitor: Visitor) {
      visitor.visit(this)
    }
  }

  class Block(val statements: List<Stmt>) : Stmt {
    override fun accept(visitor: Visitor) {
      visitor.visit(this)
    }
  }

  class If(val condition: Expr, val thenBranch: Stmt, val elseBranch: Stmt, val token: Token) :
      Stmt {
    override fun accept(visitor: Visitor) {
      visitor.visit(this)
    }
  }

  class While(val condition: Expr, val block: Stmt, val token: Token) : Stmt {
    override fun accept(visitor: Visitor) {
      visitor.visit(this)
    }
  }

  class Return(val value: Expr) : Stmt {
    override fun accept(visitor: Visitor) {
      visitor.visit(this)
    }

    class ReturnValue(val value: LoxObject) : Exception()
  }

  class Function(val name: String, val body: Stmt.Block, val params: List<Variable>) : Stmt {
    override fun accept(visitor: Stmt.Visitor) {
      return visitor.visit(this)
    }

    override fun toString() = "Fn<$name>"
  }
}

interface Expr {
  interface Visitor {
    fun visit(expr: Literal): Literal
    fun visit(unary: Unary): Literal
    fun visit(binary: Binary): Literal
    fun visit(grouping: Grouping): LoxObject
    fun visit(variable: Variable): LoxObject
    fun visit(assign: Assign): LoxObject
    fun visit(logicalExpression: LogicalExpression): LoxObject
    fun visit(call: Call): LoxObject
  }

  fun accept(visitor: Visitor): LoxObject

  class Assign(val variable: Variable, val right: Expr) : Expr {
    override fun accept(visitor: Visitor): LoxObject {
      return visitor.visit(this)
    }
  }
}

sealed class Literal : Expr, LoxObject {
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
  override fun accept(visitor: Expr.Visitor): LoxObject {
    return visitor.visit(this)
  }
}

data class Variable(val token: Token) : Expr {
  override fun accept(visitor: Expr.Visitor): LoxObject {
    return visitor.visit(this)
  }

  val identifier = token.lexeme
}

class LogicalExpression(val left: Expr, val type: Token, val right: Expr) : Expr {
  override fun accept(visitor: Expr.Visitor): LoxObject {
    return visitor.visit(this)
  }
}

class Call(val callable: Expr, val paren: Token, val args: List<Expr>) : Expr {
  override fun accept(visitor: Expr.Visitor): LoxObject {
    return visitor.visit(this)
  }
}
