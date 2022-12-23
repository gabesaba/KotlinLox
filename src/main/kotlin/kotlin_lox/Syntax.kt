package kotlin_lox

interface Expr

sealed interface Primary : Expr
data class LoxNumber(val value: Double) : Primary
data class LoxString(val value: String) : Primary
data class LoxBoolean(val value: Boolean) : Primary
object LoxNil: Primary {
    override fun toString() = "LoxNil"
}

fun parsePrimary(token: Token): Primary {
    return when (token.type) {
        TokenType.NUMBER -> LoxNumber(token.lexeme.toDouble())
        TokenType.STRING -> LoxString(token.lexeme.substring(1 until token.lexeme.length-1))
        TokenType.TRUE -> LoxBoolean(true)
        TokenType.FALSE -> LoxBoolean(false)
        TokenType.NIL -> LoxNil
        else -> LoxNil
    }
}

data class Unary(val operator: Token, val operand: Expr) : Expr
data class Binary(val operator: Token, val left: Expr, val right: Expr) : Expr
data class Parentheses(val expr: Expr) : Expr
