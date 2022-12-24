package kotlin_lox

sealed interface ParseResult
class Success(val parseTree: Expr): ParseResult
object Failure: ParseResult

class Parser(private val tokens: List<Token>) {
  private var current = 0

  fun parse(): List<Stmt> {
    val statements = mutableListOf<Stmt>()
    while (!isAtEnd()) {
      statements.add(statement())
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
    throw reportParseError(previous(), message)
  }

  private fun statement(): Stmt {
    if (match(TokenType.PRINT)) {
      return printStmt()
    }

    return expressionStatement()
  }

  private fun expressionStatement(): Expression {
    val expr = expression();
    consume(TokenType.SEMICOLON, "Expect ';' after value.")
    return Expression(expr)
  }

  private fun printStmt(): Print {
    val expr = expression();
    consume(TokenType.SEMICOLON, "Expect ';' after value.")
    return Print(expr)
  }

  private fun expression(): Expr {
    return equality()
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
    var expr = unary()
    while (match(TokenType.SLASH, TokenType.STAR)) {
      val operator = previous()
      val right = unary()
      expr = Binary(operator, expr, right)
    }
    return expr
  }

  private fun unary(): Expr {
    if (match(TokenType.BANG, TokenType.MINUS)) {
      return Unary(previous(), unary())
    }
    return primary()
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
    throw reportParseError(peek(), "Expected expression.")
  }

  private fun synchronize() {
    // TODO: page 92
  }
}

private fun reportParseError(token: Token, message: String): ParseError {
  error(token, message)
  return ParseError
}
