package kotlin_lox

sealed interface ParseResult

class Success(val parseTree: Expr) : ParseResult

object Failure : ParseResult

class Parser(private val tokens: List<Token>) {
  private var current = 0

  fun parse(): List<Stmt> {
    val statements = mutableListOf<Stmt>()
    while (!isAtEnd()) {
      try {
        statements.add(declaration())
      } catch (e: ParseError) {
        return emptyList()
      }
    }
    return statements
  }

  fun parseExpression(): ParseResult {
    return try {
      // TODO: Make sure all tokens consumed?
      Success(expression())
    } catch (parseError: ParseError) {
      Failure
    }
  }

  private fun match(vararg types: TokenType): Boolean {
    for (type in types) {
      if (check(type)) {
        advance()
        return true
      }
    }
    return false
  }

  private fun check(type: TokenType): Boolean {
    if (isAtEnd()) {
      return false
    }
    return peek().type == type
  }

  private fun isAtEnd(): Boolean {
    return peek().type == TokenType.EOF
  }

  private fun peek(): Token {
    return tokens[current]
  }

  private fun previous(): Token {
    return tokens[current - 1]
  }

  private fun advance(): Token {
    if (!isAtEnd()) {
      ++current
    }
    return previous()
  }

  private fun consume(tokenType: TokenType, message: String): Token {
    if (check(tokenType)) {
      return advance()
    }
    throw reportParseError(DebugInfo(previous()), message)
  }

  private fun declaration(): Stmt {
    if (match(TokenType.VAR)) {
      return varDeclaration()
    }
    if (match(TokenType.FUN)) {
      return function("function")
    }
    return statement()
  }

  private fun statement(): Stmt {
    if (match(TokenType.PRINT)) {
      return printStmt()
    }
    if (match(TokenType.LEFT_BRACE)) {
      return block()
    }
    if (match(TokenType.IF)) {
      return ifStatement()
    }
    if (match(TokenType.WHILE)) {
      return whileStatement()
    }
    if (match(TokenType.FOR)) {
      return forLoop()
    }
    if (match(TokenType.RETURN)) {
      return returnStatement()
    }

    return expressionStatement()
  }

  private fun function(kind: String): Stmt {
    val functionNameToken = consume(TokenType.IDENTIFIER, "Expect name.")

    consume(TokenType.LEFT_PAREN, "Expect '(' after $kind name.")
    val params = mutableListOf<Variable>()

    if (!check(TokenType.RIGHT_PAREN)) {
      do {
        val param = consume(TokenType.IDENTIFIER, "Expect parameter name.")
        params.add(Variable(param))
      } while (match(TokenType.COMMA))
    }
    consume(TokenType.RIGHT_PAREN, "Expect ')' after parameters.")
    consume(TokenType.LEFT_BRACE, "Expect '{' before $kind body.")
    val body = block()
    return Stmt.Function(functionNameToken, body, params)
  }

  private fun returnStatement(): Stmt {
    val returnToken = previous()
    val expr =
        if (!check(TokenType.SEMICOLON)) {
          expression()
        } else {
          LoxNil
        }
    consume(TokenType.SEMICOLON, "Expect ';' after return value.")
    return Stmt.Return(expr, returnToken)
  }

  private fun expressionStatement(): Stmt.Expression {
    val expr = expression()
    consume(TokenType.SEMICOLON, "Expect ';' after value.")
    return Stmt.Expression(expr)
  }

  private fun printStmt(): Stmt.Print {
    val expr = expression()
    consume(TokenType.SEMICOLON, "Expect ';' after value.")
    return Stmt.Print(expr)
  }

  private fun block(): Stmt.Block {
    val statements = mutableListOf<Stmt>()

    while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
      statements.add(declaration())
    }

    consume(TokenType.RIGHT_BRACE, "Expect '} after block.")
    return Stmt.Block(statements)
  }

  private fun ifStatement(): Stmt.If {
    val ifToken = previous()
    consume(TokenType.LEFT_PAREN, "Expect '(' after if.")
    val expr = expression()
    consume(TokenType.RIGHT_PAREN, "Expect ')' after if statement's expression")

    val thenBlock = statement()
    val elseBlock =
        if (match(TokenType.ELSE)) {
          statement()
        } else {
          Stmt.Expression(LoxNil)
        }
    return Stmt.If(expr, thenBlock, elseBlock, ifToken)
  }

  private fun whileStatement(): Stmt.While {
    val whileToken = previous()
    consume(TokenType.LEFT_PAREN, "Expect '(' after while.")
    val expr = expression()
    consume(TokenType.RIGHT_PAREN, "Expect ')' after while statement's expression")

    val thenBlock = statement()
    return Stmt.While(expr, thenBlock, whileToken)
  }

  private fun forLoop(): Stmt {
    val forToken = previous()
    consume(TokenType.LEFT_PAREN, "Expect '(' after for.")
    val initializer =
        if (match(TokenType.SEMICOLON)) {
          Stmt.Expression(LoxNil)
        } else if (match(TokenType.VAR)) {
          varDeclaration()
        } else {
          expressionStatement()
        }
    val condition =
        if (check(TokenType.SEMICOLON)) {
          LoxBoolean(true)
        } else {
          expression()
        }
    consume(TokenType.SEMICOLON, "Expect ';' after loop condition.")
    val increment =
        if (check(TokenType.RIGHT_PAREN)) {
          LoxNil
        } else {
          expression()
        }
    consume(TokenType.RIGHT_PAREN, "Expect ')' after for clauses.")

    // The body of the for-loop is executed, then the increment.
    val body = Stmt.Block(listOf(statement(), Stmt.Expression(increment)))

    return Stmt.Block(listOf(initializer, Stmt.While(condition, body, forToken)))
  }

  private fun varDeclaration(): Stmt.Var {
    val identifier = consume(TokenType.IDENTIFIER, "Expect variable name.")
    val initializer =
        if (match(TokenType.EQUAL)) {
          expression()
        } else {
          null
        }
    consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.")
    return Stmt.Var(identifier, initializer)
  }

  private fun expression(): Expr {
    return assignment()
  }

  private fun assignment(): Expr {
    val value = or()
    if (match(TokenType.EQUAL)) {
      val equals = previous()
      if (value is Variable) {
        return Expr.Assign(value, assignment())
      }
      error(DebugInfo(equals), "Invalid assignment target.")
    }
    return value
  }

  private fun or(): Expr {
    val value = and()
    if (match(TokenType.OR)) {
      val type = previous()
      return LogicalExpression(value, type, expression())
    }
    return value
  }

  private fun and(): Expr {
    val value = equality()
    if (match(TokenType.AND)) {
      val type = previous()
      return LogicalExpression(value, type, expression())
    }
    return value
  }

  private fun equality(): Expr {
    var expr = comparison()
    while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
      val operator = previous()
      val right = comparison()
      expr = Binary(operator, expr, right)
    }
    return expr
  }

  private fun comparison(): Expr {
    var expr = term()
    while (match(
        TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS_EQUAL, TokenType.LESS)) {
      val operator = previous()
      val right = term()
      expr = Binary(operator, expr, right)
    }
    return expr
  }

  private fun term(): Expr {
    var expr = factor()
    while (match(TokenType.MINUS, TokenType.PLUS)) {
      val operator = previous()
      val right = factor()
      expr = Binary(operator, expr, right)
    }
    return expr
  }

  private fun factor(): Expr {
    var expr = prefixUnary()
    while (match(TokenType.SLASH, TokenType.STAR)) {
      val operator = previous()
      val right = prefixUnary()
      expr = Binary(operator, expr, right)
    }
    return expr
  }

  private fun prefixUnary(): Expr {
    if (match(TokenType.BANG)) {
      return Unary.Not(previous(), primary())
    }
    if (match(TokenType.MINUS)) {
      return Unary.Negate(previous(), primary())
    }
    if (match(TokenType.PLUS_PLUS)) {
      return incrementOrDecrement(previous(), primary(), Unary::Increment)
    }
    if (match(TokenType.MINUS_MINUS)) {
      return incrementOrDecrement(previous(), primary(), Unary::Decrement)
    }
    return postFixUnary()
  }

  private fun incrementOrDecrement(
    token: Token,
    target: Expr,
    constructor: (Token, Expr) -> Unary
  ): Expr {
    if (target !is Variable) {
      throw reportParseError(DebugInfo(token), "Expect variable. Got $target.")
    }
    return Expr.Assign(target, constructor(token, target))
  }

  private fun postFixUnary(): Expr {
    val expr = primary()
    return when (peek().type) {
      TokenType.PLUS_PLUS -> incrementOrDecrement(advance(), functionCall(expr), Unary::PostfixIncrement)
      TokenType.MINUS_MINUS -> incrementOrDecrement(advance(), functionCall(expr), Unary::PostfixDecrement)
      else -> functionCall(expr)
    }
  }

  private fun functionCall(expr: Expr): Expr {
    if (match(TokenType.LEFT_PAREN)) {
      val args = parseArgs()
      val paren = consume(TokenType.RIGHT_PAREN, "Expect ')' to close function call.;")
      val fn = Call(expr, paren, args)
      return functionCall(fn)
    }
    return expr
  }

  private fun parseArgs(): List<Expr> {
    if (check(TokenType.RIGHT_PAREN)) {
      return emptyList()
    }
    val args = mutableListOf<Expr>()
    do {
      args.add(expression())
    } while (match(TokenType.COMMA))
    if (args.size >= 255) {
      error(DebugInfo(previous()), "Can't have more than 255 arguments.")
      return args.subList(0, 255)
    }
    return args
  }

  private fun primary(): Expr {
    if (match(TokenType.NUMBER, TokenType.STRING, TokenType.TRUE, TokenType.FALSE, TokenType.NIL)) {
      return parsePrimary(previous())
    }
    if (match(TokenType.LEFT_PAREN)) {
      val expr = expression()
      consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.")
      return Grouping(expr)
    }
    if (match(TokenType.IDENTIFIER)) {
      return Variable(previous())
    }
    throw reportParseError(DebugInfo(peek()), "Expected expression.")
  }

  private fun synchronize() {
    // TODO: page 92
  }
}

private fun reportParseError(debugInfo: DebugInfo, message: String): ParseError {
  error(debugInfo, message)
  return ParseError
}
