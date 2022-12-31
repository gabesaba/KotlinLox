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

  class Var(token: Token, val expr: Expr?) : Stmt, Debuggable {
    override val debugInfo = DebugInfo(token)
    val variable = Variable(token)

    override fun accept(visitor: Visitor) {
      visitor.visit(this)
    }
  }

  class Block(val statements: List<Stmt>) : Stmt {
    override fun accept(visitor: Visitor) {
      visitor.visit(this)
    }
  }

  class If(val condition: Expr, val thenBranch: Stmt, val elseBranch: Stmt, token: Token) :
      Stmt, Debuggable {
    override val debugInfo = DebugInfo(token)

    override fun accept(visitor: Visitor) {
      visitor.visit(this)
    }
  }

  class While(val condition: Expr, val block: Stmt, token: Token) : Stmt, Debuggable {
    override val debugInfo = DebugInfo(token)

    override fun accept(visitor: Visitor) {
      visitor.visit(this)
    }
  }

  class Return(val value: Expr, token: Token) : Stmt, Debuggable {
    override val debugInfo = DebugInfo(token)

    override fun accept(visitor: Visitor) {
      visitor.visit(this)
    }

    class ReturnValue(val value: LoxObject) : Exception()
  }

  class Function(token: Token, val body: Stmt.Block, val params: List<Variable>) :
      Stmt, Debuggable {
    val variable = Variable(token)
    val name = variable.identifier
    override val debugInfo = DebugInfo(token)

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

sealed class Unary(operator: Token, val operand: Expr) : Expr, Debuggable {

  class Not(operator: Token, operand: Expr) : Unary(operator, operand)
  class Negate(operator: Token, operand: Expr) : Unary(operator, operand)
  class Increment(operator: Token, operand: Expr) : Unary(operator, operand)
  class Decrement(operator: Token, operand: Expr) : Unary(operator, operand)
  class PostfixIncrement(operator: Token, operand: Expr) : Unary(operator, operand)
  class PostfixDecrement(operator: Token, operand: Expr) : Unary(operator, operand)

  override val debugInfo = DebugInfo(operator)

  override fun accept(visitor: Expr.Visitor): Literal {
    return visitor.visit(this)
  }
}

data class Binary(val operator: Token, val left: Expr, val right: Expr) : Expr, Debuggable {
  override val debugInfo = DebugInfo(operator)

  override fun accept(visitor: Expr.Visitor): Literal {
    return visitor.visit(this)
  }
}

data class Grouping(val expr: Expr) : Expr {
  override fun accept(visitor: Expr.Visitor): LoxObject {
    return visitor.visit(this)
  }
}

class Variable(token: Token) : Expr, Debuggable {
  override val debugInfo = DebugInfo(token)

  override fun accept(visitor: Expr.Visitor): LoxObject {
    return visitor.visit(this)
  }

  override fun toString(): String {
    return "var '$identifier'"
  }

  override fun equals(other: Any?): Boolean {
    if (other !is Variable) return false
    return this.identifier == other.identifier
  }

  override fun hashCode(): Int {
    return identifier.hashCode()
  }

  val identifier = token.lexeme
}

class LogicalExpression(val left: Expr, val type: Token, val right: Expr) : Expr, Debuggable {
  override val debugInfo = DebugInfo(type)

  override fun accept(visitor: Expr.Visitor): LoxObject {
    return visitor.visit(this)
  }
}

class Call(val callable: Expr, paren: Token, val args: List<Expr>) : Expr, Debuggable {
  override val debugInfo = DebugInfo(paren)

  override fun accept(visitor: Expr.Visitor): LoxObject {
    return visitor.visit(this)
  }
}
