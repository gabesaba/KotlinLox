package kotlin_lox

data class Token(val type: TokenType, val lexeme: String, val literal: Literal, val line: Int) {
  override fun toString(): String {
    return "$type $lexeme $literal"
  }
}

sealed class Literal

object NoValue : Literal() {
  override fun toString(): String {
    return "NoValue"
  }
}

data class LoxNumber(val number: Double) : Literal()

data class LoxString(val string: String) : Literal()

enum class TokenType {
  // Single-character tokens.
  LEFT_PAREN,
  RIGHT_PAREN,
  LEFT_BRACE,
  RIGHT_BRACE,
  COMMA,
  DOT,
  MINUS,
  PLUS,
  SEMICOLON,
  SLASH,
  STAR,

  // One or two-character tokens.
  BANG,
  BANG_EQUAL,
  EQUAL,
  EQUAL_EQUAL,
  GREATER,
  GREATER_EQUAL,
  LESS,
  LESS_EQUAL,

  // Literals.
  IDENTIFIER,
  STRING,
  NUMBER,

  // Keywords.
  AND,
  CLASS,
  ELSE,
  FALSE,
  FUN,
  FOR,
  IF,
  NIL,
  OR,
  PRINT,
  RETURN,
  SUPER,
  THIS,
  TRUE,
  VAR,
  WHILE,

  // Etc.
  EOF,
}
