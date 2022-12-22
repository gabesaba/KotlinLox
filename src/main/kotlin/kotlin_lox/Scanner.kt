package kotlin_lox

private class SourceIterator(private val source: String) {
  private var start = 0
  private var current = 0
  var line = 1

  fun advance(): Char {
    return source[current++]
  }

  fun peek(): Char? {
    return if (isAtEnd()) null else source[current]
  }

  fun peekNext(): Char? {
    return if (current + 1 >= source.length) null else source[current + 1]
  }

  fun match(char: Char): Boolean {
    if (isAtEnd()) {
      return false
    }
    if (peek() != char) {
      return false
    }
    current++
    return true
  }

  fun closeLexeme(): String {
    val lexeme = source.substring(start until current)
    start = current
    return lexeme
  }

  fun isAtEnd(): Boolean {
    return current >= source.length
  }
}

class Scanner(source: String) {
  private val sourceIterator = SourceIterator(source)

  fun scanTokens(): List<Token> {
    val tokens = mutableListOf<Token>()

    while (!sourceIterator.isAtEnd()) {
      sourceIterator.closeLexeme()
      val token = scanToken(sourceIterator)
      if (token != null) {
        tokens.add(token)
      }
    }
    return tokens
  }
}

private fun scanToken(source: SourceIterator): Token? {
  val c = source.advance()
  val tokenType =
      when (c) {
        ' ' -> return null
        '\t' -> return null
        '\r' -> return null
        '\n' -> {
          source.line++
          return null
        }
        '(' -> TokenType.LEFT_PAREN
        ')' -> TokenType.RIGHT_PAREN
        '{' -> TokenType.LEFT_BRACE
        '}' -> TokenType.RIGHT_BRACE
        ',' -> TokenType.COMMA
        '.' -> TokenType.DOT
        '-' -> TokenType.MINUS
        '+' -> TokenType.PLUS
        ';' -> TokenType.SEMICOLON
        '*' -> TokenType.STAR
        '!' -> if (source.match('=')) TokenType.BANG_EQUAL else TokenType.BANG
        '<' -> if (source.match('=')) TokenType.LESS_EQUAL else TokenType.LESS
        '>' -> if (source.match('=')) TokenType.GREATER_EQUAL else TokenType.GREATER
        '=' -> if (source.match('=')) TokenType.EQUAL_EQUAL else TokenType.EQUAL
        '/' -> {
          if (source.match('/')) {
            while (source.peek() != '\n' && !source.isAtEnd()) {
              source.advance()
            }
            return null
          } else TokenType.SLASH
        }
        '"' -> {
          return string(source)
        }
        in '0'..'9' -> return number(source)
        else -> {
          if (c.isAlpha()) {
            return identifierOrKeyword(source)
          }
          error(source.line, "Unrecognized Token")
          return null
        }
      }
  return Token(tokenType, source.closeLexeme(), NoValue, source.line)
}

private fun Char.isAlpha(): Boolean {
  return this.isLetter() || this == '_'
}

private fun Char.isAlphaNumeric(): Boolean {
  return this.isAlpha() || this.isDigit()
}

private fun string(source: SourceIterator): Token? {
  while (source.peek() != '"' && !source.isAtEnd()) {
    if (source.peek() == '\n') source.line++
    source.advance()
  }
  if (source.isAtEnd()) {
    error(source.line, "Unterminated string.")
    return null
  }

  source.advance()

  val lexeme = source.closeLexeme()
  val loxString = LoxString(lexeme.substring(1 until lexeme.length - 1))
  return Token(TokenType.STRING, lexeme, loxString, source.line)
}

private fun number(source: SourceIterator): Token {
  while (source.peek()?.isDigit() == true) source.advance()

  if (source.peek() == '.' && source.peekNext()?.isDigit() == true) {
    source.advance()

    while (source.peek()?.isDigit() == true) source.advance()
  }

  val lexeme = source.closeLexeme()

  return Token(TokenType.NUMBER, lexeme, LoxNumber(lexeme.toDouble()), source.line)
}

private fun identifierOrKeyword(source: SourceIterator): Token {
  while (source.peek()?.isAlphaNumeric() == true) source.advance()

  val lexeme = source.closeLexeme()
  val tokenType = resolveIdentifierOrKeyword(lexeme)
  return Token(tokenType, lexeme, NoValue, source.line)
}

private fun resolveIdentifierOrKeyword(lexeme: String): TokenType {
  return when (lexeme) {
    "and" -> TokenType.AND
    "class" -> TokenType.CLASS
    "else" -> TokenType.ELSE
    "false" -> TokenType.FALSE
    "for" -> TokenType.FOR
    "fun" -> TokenType.FUN
    "if" -> TokenType.IF
    "nil" -> TokenType.NIL
    "or" -> TokenType.OR
    "print" -> TokenType.PRINT
    "return" -> TokenType.RETURN
    "super" -> TokenType.SUPER
    "this" -> TokenType.THIS
    "true" -> TokenType.TRUE
    "var" -> TokenType.VAR
    "while" -> TokenType.WHILE
    else -> TokenType.IDENTIFIER
  }
}
