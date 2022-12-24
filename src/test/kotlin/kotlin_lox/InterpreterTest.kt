package kotlin_lox

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class InterpreterTest {

    @Test
    fun testAddition() {
        assertEquals(eval("1 + 2 + 3"), LoxNumber(6.0))
    }

    @Test
    fun testSubtraction() {
        assertEquals(eval("-1 - 2 - 3"), LoxNumber(-6.0))
    }

    @Test
    fun testUnaryPrecedence() {
        assertEquals(eval("-1 + -3 - -27"), LoxNumber(23.0))
    }

    @Test
    fun testDivision() {
        assertEquals(eval("10 / 2"), LoxNumber(5.0))
    }

    @Test
    fun testMultiplicationPrecedence() {
        assertEquals(eval("1 + 2 * 3"), LoxNumber(7.0))
        assertEquals(eval("1 * 2 + 3"), LoxNumber(5.0))
    }

    @Test
    fun testDivisionPrecedence() {
        assertEquals(eval("1 - 2 / 4"), LoxNumber(0.5))
        assertEquals(eval("1 / 2 + 4"), LoxNumber(4.5))
    }

    @Test
    fun testGrouping() {
        assertEquals(eval("(1 + 2) * 3"), LoxNumber(9.0))
        assertEquals(eval("4 * (2 + 3)"), LoxNumber(20.0))
    }

    @Test
    fun testGreaterThan() {
        assertEquals(eval("0.0 > 0.0"), LoxBoolean(false))
        assertEquals(eval("0.1 > 0.0"), LoxBoolean(true))
        assertEquals(eval("0.0 > 0.1"), LoxBoolean(false))
    }

    @Test
    fun testGreaterThanEqual() {
        assertEquals(eval("0.0 >= 0.0"), LoxBoolean(true))
        assertEquals(eval("0.1 >= 0.0"), LoxBoolean(true))
        assertEquals(eval("0.0 >= 0.1"), LoxBoolean(false))
    }

    @Test
    fun testLessThan() {
        assertEquals(eval("0.0 < 0.0"), LoxBoolean(false))
        assertEquals(eval("0.1 < 0.0"), LoxBoolean(false))
        assertEquals(eval("0.0 < 0.1"), LoxBoolean(true))
    }

    @Test
    fun testLessThanEquals() {
        assertEquals(eval("0.0 <= 0.0"), LoxBoolean(true))
        assertEquals(eval("0.1 <= 0.0"), LoxBoolean(false))
        assertEquals(eval("0.0 <= 0.1"), LoxBoolean(true))
    }

    @Test
    fun testStringConcatenation() {
        assertEquals(eval("""
            "Hello" + " " + "World"
        """.trimIndent()), LoxString("Hello World"))
    }

    @Test
    fun testNegation() {
        assertEquals(eval("- (5 + 5)"), LoxNumber(-10.0))
        assertEquals(eval("--10"), LoxNumber(10.0))
        assertEquals(eval("---10"), LoxNumber(-10.0))
    }

    @Test
    fun testNegationFailsWithNonNumber() {
        assertNull(eval("- false"))
        assertNull(eval("- nil"))
    }

    @Test
    fun testNot() {
        assertEquals(eval("!true"), LoxBoolean(false))
        assertEquals(eval("!false"), LoxBoolean(true))
    }

    @Test
    fun testNotFailsWithNonBoolean() {
        assertNull(eval("!10"))
        assertNull(eval("!nil"))
    }

    @Test
    fun testChainedEqualityProducesFalse() {
        // 5 == 5 -> false
        // false == 5 -> false
        assertEquals(eval("5 == 5 == 5"), LoxBoolean(false))
        // false == false -> true
        assertEquals(eval("5 == 5 == 5 == false"), LoxBoolean(true))
    }

    @Test
    fun testBooleanEquality() {
        assertEquals(eval("true == true"), LoxBoolean(true))
        assertEquals(eval("true == false"), LoxBoolean(false))
        assertEquals(eval("false == false"), LoxBoolean(true))
    }

    @Test
    fun testBooleanInequality() {
        assertEquals(eval("true != true"), LoxBoolean(false))
        assertEquals(eval("true != false"), LoxBoolean(true))
        assertEquals(eval("false != false"), LoxBoolean(false))
    }

    @Test
    fun testNilEquality() {
        assertEquals(eval("nil == nil"), LoxBoolean(true))
    }

    @Test
    fun testNilDoesntEqualOtherTypes() {
        assertEquals(eval("nil == 5.0"), LoxBoolean(false))
        assertEquals(eval("nil == false"), LoxBoolean(false))
    }

    @Test
    fun testNumberEquality() {
        assertEquals(eval("0.0 == 0.0"), LoxBoolean(true))
        assertEquals(eval("-5.0 == 0.0"), LoxBoolean(false))
        assertEquals(eval("5.0 == 4.9"), LoxBoolean(false))
    }

    @Test
    fun testNumberInequality() {
        assertEquals(eval("0.0 != 0.0"), LoxBoolean(false))
        assertEquals(eval("-5.0 != 0.0"), LoxBoolean(true))
        assertEquals(eval("5.0 != 4.9"), LoxBoolean(true))
    }

    @Test
    fun fuzzNumberComparison() {
        for (a in -10 until 10) {
            for (b in -10 until 10) {
                val left = a / 10.0
                val right = b / 10.0
                assertEquals(eval("$left == $right"), LoxBoolean(left == right))
                assertEquals(eval("$left != $right"), LoxBoolean(left != right))
                assertEquals(eval("$left > $right"), LoxBoolean(left > right))
                assertEquals(eval("$left >= $right"), LoxBoolean(left >= right))
                assertEquals(eval("$left < $right"), LoxBoolean(left < right))
                assertEquals(eval("$left <= $right"), LoxBoolean(left <= right))
            }
        }
    }
  private fun eval(expression: String): Literal? {
    val testKey = "test_output"
    val tokens = Scanner("var $testKey = $expression;").scanTokens()
    val statements = Parser(tokens).parse()
    val env = Environment()
    val interpreter = Interpreter(env)
    interpreter.interpret(statements)
    return env.get(testKey)
  }
}
