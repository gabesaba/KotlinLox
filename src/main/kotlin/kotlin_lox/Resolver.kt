package kotlin_lox

import kotlin_lox.ResolverError.*

typealias Scope = MutableMap<Variable, Resolver.VariableState>

class Resolver : Expr.Visitor, Stmt.Visitor {

  private val scopes = mutableListOf<Scope>(mutableMapOf())
  private var currentFunction = FunctionType.None

  private val errors = mutableListOf<ResolverError>()

  enum class VariableState {
    Declared,
    Defined,
    Read,
  }

  private enum class FunctionType {
    None,
    Function
  }

  fun resolve(statements: List<Stmt>): List<ResolverError> {
    for (statement in statements) {
      resolve(statement)
    }
    return errors
  }

  override fun visit(print: Stmt.Print) {
    resolve(print.expr)
  }

  override fun visit(expression: Stmt.Expression) {
    resolve(expression.expr)
  }

  override fun visit(v: Stmt.Var) {
    declare(v.variable)

    if (v.expr == null) return

    resolve(v.expr)
    define(v.variable)
  }

  override fun visit(block: Stmt.Block) {
    beginScope()
    for (statement in block.statements) {
      resolve(statement)
    }
    endScope()
  }

  override fun visit(ifStmt: Stmt.If) {
    resolve(ifStmt.condition)
    resolve(ifStmt.thenBranch)
    resolve(ifStmt.elseBranch)
  }

  override fun visit(whileStmt: Stmt.While) {
    resolve(whileStmt.condition)
    resolve(whileStmt.block)
  }

  override fun visit(returnStmt: Stmt.Return) {
    if (currentFunction == FunctionType.None) {
      logResolverError(ReturnOutsideOfFunction, returnStmt, "Can't return from top-level code.")
    }
    resolve(returnStmt.value)
  }

  override fun visit(function: Stmt.Function) {
    declare(function.variable)
    define(function.variable)
    resolveFunction(function, FunctionType.Function)
  }

  override fun visit(expr: Literal): Literal {
    return LoxNil
  }

  override fun visit(unary: Unary): Literal {
    resolve(unary.operand)
    return LoxNil
  }

  override fun visit(binary: Binary): Literal {
    resolve(binary.left)
    resolve(binary.right)
    return LoxNil
  }

  override fun visit(grouping: Grouping): LoxObject {
    resolve(grouping.expr)
    return LoxNil
  }

  override fun visit(variable: Variable): LoxObject {
    val scope = getVariableScope(variable)

    when (scope?.get(variable)) {
      null -> {
        // TODO: Determine a way to tell Resolver about globals.
        // logResolverError(VariableUndefined, variable.token.line,
        // "Unable to resolve identifier ${variable.identifier}.")
      }
      VariableState.Declared -> {
        logResolverError(
            VariableReadInInitializer, variable, "Tried to access $variable before definition.")
      }
      VariableState.Defined -> {
        scope[variable] = VariableState.Read
      }
      VariableState.Read -> {}
    }
    return LoxNil
  }

  override fun visit(assign: Expr.Assign): LoxObject {
    resolve(assign.right)
    define(assign.variable)
    return LoxNil
  }

  override fun visit(logicalExpression: LogicalExpression): LoxObject {
    resolve(logicalExpression.left)
    resolve(logicalExpression.right)
    return LoxNil
  }

  override fun visit(call: Call): LoxObject {
    resolve(call.callable)
    for (arg in call.args) {
      resolve(arg)
    }
    return LoxNil
  }

  private fun resolve(statement: Stmt) {
    statement.accept(this)
  }

  private fun resolve(expr: Expr) {
    expr.accept(this)
  }

  private fun resolveFunction(function: Stmt.Function, functionType: FunctionType) {
    val enclosingFunction = currentFunction
    currentFunction = functionType

    beginScope()
    for (param in function.params) {
      declare(param)
      define(param)
    }
    resolve(function.body)
    endScope()

    currentFunction = enclosingFunction
  }

  private fun declare(variable: Variable) {
    if (scopes.last().containsKey(variable)) {
      logResolverError(
          ResolverError.VariableDefinedTwice,
          variable,
          "Already a variable with this name in this scope.")
    }
    scopes.last()[variable] = VariableState.Declared
  }

  private fun define(variable: Variable) {
    val scope = getVariableScope(variable)
    if (scope == null) {
      logResolverError(VariableUndefined, variable, "Assigning to undefined variable.")
      return
    }
    if (scope[variable] == VariableState.Declared) {
      scope[variable] = VariableState.Defined
    }
  }

  private fun getVariableScope(variable: Variable): Scope? {
    for (scope in scopes.reversed()) {
      if (scope.containsKey(variable)) {
        return scope
      }
    }
    return null
  }

  private fun beginScope() {
    scopes.add(mutableMapOf())
  }

  private fun endScope() {
    val scope = scopes.removeLast()

    for ((key, value) in scope) {
      when (value) {
        VariableState.Declared -> logResolverError(VariableNeverSet, key, "$key never set.")
        VariableState.Defined -> logResolverError(VariableNeverRead, key, "$key never read.")
        VariableState.Read -> {}
      }
    }
  }

  private fun logResolverError(error: ResolverError, debuggable: Debuggable, message: String) {
    errors.add(error)
    error(debuggable.debugInfo, message)
  }
}
