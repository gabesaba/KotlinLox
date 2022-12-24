package kotlin_lox

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class InterpreterTest {

    @Test
    fun testAddition() {
        assertEquals(interpret("1 + 2 + 3"), LoxNumber(6.0))
    }

    @Test
    fun testSubtraction() {
        assertEquals(interpret("-1 - 2 - 3"), LoxNumber(-6.0))
    }

    @Test
    fun testUnaryPrecedence() {
        assertEquals(interpret("-1 + -3 - -27"), LoxNumber(23.0))
    }

    @Test
    fun testDivision() {
        assertEquals(interpret("10 / 2"), LoxNumber(5.0))
    }

    @Test
    fun testMultiplicationPrecedence() {
        assertEquals(interpret("1 + 2 * 3"), LoxNumber(7.0))
        assertEquals(interpret("1 * 2 + 3"), LoxNumber(5.0))
    }

    @Test
    fun testDivisionPrecedence() {
        assertEquals(interpret("1 - 2 / 4"), LoxNumber(0.5))
        assertEquals(interpret("1 / 2 + 4"), LoxNumber(4.5))
    }

    @Test
    fun testGrouping() {
        assertEquals(interpret("(1 + 2) * 3"), LoxNumber(9.0))
        assertEquals(interpret("4 * (2 + 3)"), LoxNumber(20.0))
    }

    @Test
    fun testGreaterThan() {
        assertEquals(interpret("0.0 > 0.0"), LoxBoolean(false))
        assertEquals(interpret("0.1 > 0.0"), LoxBoolean(true))
        assertEquals(interpret("0.0 > 0.1"), LoxBoolean(false))
    }

    @Test
    fun testGreaterThanEqual() {
        assertEquals(interpret("0.0 >= 0.0"), LoxBoolean(true))
        assertEquals(interpret("0.1 >= 0.0"), LoxBoolean(true))
        assertEquals(interpret("0.0 >= 0.1"), LoxBoolean(false))
    }

    @Test
    fun testLessThan() {
        assertEquals(interpret("0.0 < 0.0"), LoxBoolean(false))
        assertEquals(interpret("0.1 < 0.0"), LoxBoolean(false))
        assertEquals(interpret("0.0 < 0.1"), LoxBoolean(true))
    }

    @Test
    fun testLessThanEquals() {
        assertEquals(interpret("0.0 <= 0.0"), LoxBoolean(true))
        assertEquals(interpret("0.1 <= 0.0"), LoxBoolean(false))
        assertEquals(interpret("0.0 <= 0.1"), LoxBoolean(true))
    }

    @Test
    fun testStringConcatenation() {
        assertEquals(interpret("""
            "Hello" + " " + "World"
        """.trimIndent()), LoxString("Hello World"))
    }

    @Test
    fun testNegation() {
        assertEquals(interpret("- (5 + 5)"), LoxNumber(-10.0))
        assertEquals(interpret("--10"), LoxNumber(10.0))
        assertEquals(interpret("---10"), LoxNumber(-10.0))
    }

    @Test
    fun testNegationFailsWithNonNumber() {
        assertNull(interpret("- false"))
        assertNull(interpret("- nil"))
    }

    @Test
    fun testNot() {
        assertEquals(interpret("!true"), LoxBoolean(false))
        assertEquals(interpret("!false"), LoxBoolean(true))
    }

    @Test
    fun testNotFailsWithNonBoolean() {
        assertNull(interpret("!10"))
        assertNull(interpret("!nil"))
    }

    @Test
    fun testChainedEqualityProducesFalse() {
        // 5 == 5 -> false
        // false == 5 -> false
        assertEquals(interpret("5 == 5 == 5"), LoxBoolean(false))
        // false == false -> true
        assertEquals(interpret("5 == 5 == 5 == false"), LoxBoolean(true))
    }

    @Test
    fun testBooleanEquality() {
        assertEquals(interpret("true == true"), LoxBoolean(true))
        assertEquals(interpret("true == false"), LoxBoolean(false))
        assertEquals(interpret("false == false"), LoxBoolean(true))
    }

    @Test
    fun testBooleanInequality() {
        assertEquals(interpret("true != true"), LoxBoolean(false))
        assertEquals(interpret("true != false"), LoxBoolean(true))
        assertEquals(interpret("false != false"), LoxBoolean(false))
    }

    @Test
    fun testNilEquality() {
        assertEquals(interpret("nil == nil"), LoxBoolean(true))
    }

    @Test
    fun testNilDoesntEqualOtherTypes() {
        assertEquals(interpret("nil == 5.0"), LoxBoolean(false))
        assertEquals(interpret("nil == false"), LoxBoolean(false))
    }

    @Test
    fun testNumberEquality() {
        assertEquals(interpret("0.0 == 0.0"), LoxBoolean(true))
        assertEquals(interpret("-5.0 == 0.0"), LoxBoolean(false))
        assertEquals(interpret("5.0 == 4.9"), LoxBoolean(false))
    }

    @Test
    fun testNumberInequality() {
        assertEquals(interpret("0.0 != 0.0"), LoxBoolean(false))
        assertEquals(interpret("-5.0 != 0.0"), LoxBoolean(true))
        assertEquals(interpret("5.0 != 4.9"), LoxBoolean(true))
    }

    @Test
    fun fuzzNumberComparison() {
        for (a in -10 until 10) {
            for (b in -10 until 10) {
                val left = a / 10.0
                val right = b / 10.0
                assertEquals(interpret("$left == $right"), LoxBoolean(left == right))
                assertEquals(interpret("$left != $right"), LoxBoolean(left != right))
                assertEquals(interpret("$left > $right"), LoxBoolean(left > right))
                assertEquals(interpret("$left >= $right"), LoxBoolean(left >= right))
                assertEquals(interpret("$left < $right"), LoxBoolean(left < right))
                assertEquals(interpret("$left <= $right"), LoxBoolean(left <= right))
            }
        }
    }

    private fun interpret(code: String): Literal? {
        val tokens = Scanner(code).scanTokens()
        val parse = Parser(tokens).parse() as Success
        return Interpreter().interpret(parse.parseTree)
    }
}
