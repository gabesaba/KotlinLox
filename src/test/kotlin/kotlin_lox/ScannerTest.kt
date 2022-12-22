package kotlin_lox

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin_lox.TokenType.*

class ScannerTest {
  @Test
  fun testParseLeftParen() {
    assertEquals(Token(LEFT_PAREN, "(", NoValue, 1), Scanner("(").scanTokens().first())
  }

  @Test
  fun testParseTokens() {
    val tokens = Scanner("(){},.-+;/*=!!===>>=<<=\"hello\"3.14").scanTokens()
    assertEquals(
        listOf(
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
            EQUAL,
            BANG,
            BANG_EQUAL,
            EQUAL_EQUAL,
            GREATER,
            GREATER_EQUAL,
            LESS,
            LESS_EQUAL,
            STRING,
            NUMBER,
        ),
        tokens.toTokenTypes())
  }

  @Test
  fun testParseKeywords() {
    val tokens =
        Scanner("and class else false fun for if nil or print return super this true var while")
            .scanTokens()
    print(tokens)
    assertEquals(
        listOf(
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
            WHILE),
        tokens.toTokenTypes())
  }

  @Test
  fun longestMatch() {
    val tokens = Scanner("funny fun").scanTokens()
    assertEquals(listOf(IDENTIFIER, FUN), tokens.toTokenTypes())
  }

  @Test
  fun testParseString() {
    val tokens = Scanner("\"Hello World\"").scanTokens()
    assertEquals(
        Token(STRING, "\"Hello World\"", LoxString("Hello World"), 1), tokens.first())
  }

  @Test
  fun testParseNumber() {
    val tokens = Scanner("3.14159").scanTokens()
    assertEquals(Token(NUMBER, "3.14159", LoxNumber(3.14159), 1), tokens.first())
  }

  @Test
  fun testLineNumbers() {
    val tokens = Scanner("(\n\n)").scanTokens()
    assertEquals(listOf(1, 3), tokens.map { it.line })
  }

  private fun Iterable<Token>.toTokenTypes(): Iterable<TokenType> {
    return this.map { it.type }
  }
}
