package kotlin_lox

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class InterpreterTest {

  private val env = Environment()
  private val testOutput = TestOutput()
  private val interpreter = Interpreter(env)

  @BeforeTest
  fun defineSetTestOutput() {
    env.define("setTestOutput")
    env.assign("setTestOutput", testOutput)
  }

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
    assertEquals(
        eval(
            """
            "Hello" + " " + "World"
        """
                .trimIndent()),
        LoxString("Hello World"))
  }

  @Test
  fun testNegation() {
    assertEquals(eval("- (5 + 5)"), LoxNumber(-10.0))
    assertEquals(eval("-(-10)"), LoxNumber(10.0))
    assertEquals(eval("-(-(-10))"), LoxNumber(-10.0))
  }

  @Test
  fun testNegationFailsWithNonNumber() {
    assertEquals(LoxNil, eval("- false"))
    assertEquals(LoxNil, eval("- nil"))
  }

  @Test
  fun testNot() {
    assertEquals(eval("!true"), LoxBoolean(false))
    assertEquals(eval("!false"), LoxBoolean(true))
  }

  @Test
  fun testNotFailsWithNonBoolean() {
    assertEquals(LoxNil, eval("!10"))
    assertEquals(LoxNil, eval("!nil"))
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

  @Test
  fun testIfStatement() {
    interpret("""
      if (true) {
        setTestOutput(1);
      }
      """)
    assertEquals(LoxNumber(1.0), getTestOutput())
  }

  @Test
  fun testIfStatementNotExecuted() {
    interpret(
        """
      var a = 0;
      if (false) {
        a = 1;
      }
      setTestOutput(a);
      """)
    assertEquals(LoxNumber(0.0), getTestOutput())
  }

  @Test
  fun testIfStatementWithElseBranch() {
    interpret(
        """
      var a = 0;
      if (true) {
        a = 1;
      } else {
        a = 2;
      }
      setTestOutput(a);
      """)
    assertEquals(LoxNumber(1.0), getTestOutput())
  }

  @Test
  fun testElseBranchExecuted() {
    interpret(
        """
      var a = 0;
      if (false) {
        a = 1;
      } else {
        a = 2;
      }
      setTestOutput(a);
      """)
    assertEquals(LoxNumber(2.0), getTestOutput())
  }

  @Test
  fun testShortCircuitAnd() {
    interpret(
        """
      var a = false;
      false and a = true;
      
      setTestOutput(a);
    """
            .trimIndent())
    assertEquals(LoxBoolean(false), getTestOutput())
  }

  @Test
  fun testSideEffectAnd() {
    interpret(
        """
      var a = false;
      true and a = true;
      setTestOutput(a);
    """
            .trimIndent())

    assertEquals(LoxBoolean(true), getTestOutput())
  }

  @Test
  fun testShortCircuitOr() {
    interpret(
        """
      var a = false;
      true or a = true;
      setTestOutput(a);
    """
            .trimIndent())

    assertEquals(LoxBoolean(false), getTestOutput())
  }

  @Test
  fun testSideEffectOr() {
    interpret(
        """
      var a = false;
      false or a = true;
      setTestOutput(a);
    """
            .trimIndent())

    assertEquals(LoxBoolean(true), getTestOutput())
  }

  @Test
  fun testWhile() {
    interpret(
        """
      var a = 0;
      while (a < 10) {
        a = a + 1;
      }
      setTestOutput(a);
    """
            .trimIndent())

    assertEquals(LoxNumber(10.0), getTestOutput())
  }

  @Test
  fun testWhileDoesntExecute() {
    interpret(
        """
      var a = false;
      while (false) {
        a = true;
      }
      setTestOutput(a);
    """
            .trimIndent())

    assertEquals(LoxBoolean(false), getTestOutput())
  }

  @Test
  fun testForLoop() {
    interpret(
        """
      var a = 0;

      for (var i = 0; i < 10; i = i + 1) {
        a = a + i;
      }
      setTestOutput(a);
    """
            .trimIndent())

    assertEquals(LoxNumber(45.0), getTestOutput())
  }

  @Test
  fun testForLoopWithoutInitializer() {
    interpret(
        """
      var a = 0;
      var i = 0;

      for (; i < 10; i = i + 1) {
        a = a + i;
      }
      setTestOutput(a);
    """
            .trimIndent())

    assertEquals(LoxNumber(45.0), getTestOutput())
  }

  @Test
  fun testForLoopWithoutIncrement() {
    interpret(
        """
      var a = 0;

      for (var i = 0; i < 10;) {
        a = a + i;
        i = i + 1;
      }
      setTestOutput(a);
    """
            .trimIndent())

    assertEquals(LoxNumber(45.0), getTestOutput())
  }

  @Test
  fun testForLoopInitializer() {
    interpret(
        """
      var a = 0;
      var i = "not a number!";

      for (i = 0; i < 10; i = i + 1) {
        a = a + i;
      }
      setTestOutput(a);
    """
            .trimIndent())

    assertEquals(LoxNumber(45.0), getTestOutput())
  }

  @Test
  fun testFunction() {
    interpret(
        """
      fun hello() {
        return 5;
      }
      setTestOutput(hello());
    """
            .trimIndent())

    assertEquals(LoxNumber(5.0), getTestOutput())
  }

  @Test
  fun testUnaryFunction() {
    interpret(
        """
      fun hello(x) {
        return x * x;
      }
      setTestOutput(hello(5));
    """
            .trimIndent())

    assertEquals(LoxNumber(25.0), getTestOutput())
  }

  @Test
  fun testBinaryFunction() {
    interpret(
        """
      fun power(x, y) {
        var res = 1.0;
        for (var i = 0; i < y; i = i + 1) {
          res = res * x;
        }
        return res;
      }
      setTestOutput(power(3, 4));
    """
            .trimIndent())

    assertEquals(LoxNumber(81.0), getTestOutput())
  }

  @Test
  fun testReturnNothing() {
    interpret(
        """
      fun hello() {
        var x = 0;
        while (true) {
          if (x == 100) {
            return;
          }
          x = x + 1;
        }
      }

      if (hello() == nil) {
        setTestOutput("success");
      }
      
    """
            .trimIndent())

    assertEquals(LoxString("success"), getTestOutput())
  }

  @Test
  fun testClosure() {
    interpret(
        """
      fun makeCatter() {
        var a = "";
        fun catter() {
          return a = a + "a";
        }
        return catter;
      }
      var catter = makeCatter();
      catter();
      catter();
      setTestOutput(catter());
    """
            .trimIndent())

    assertEquals(LoxString("aaa"), getTestOutput())
  }

  @Test
  fun testLexicalScope() {
    interpret(
        """
    var a = "global";
    {
      fun run() {
        setTestOutput(a);
      }
      var a = "block";
      print a;
      run();
    }
    """)

    assertEquals(LoxString("global"), getTestOutput())
  }

  @Test
  fun testPrefixIncrement() {
    val program = """
    var a = 0;
    ++a;
    ++a;
    ++a;
    setTestOutput(a);
    """

    interpret(program)

    assertEquals(LoxNumber(3.0), getTestOutput())
  }

  @Test
  fun testPrefixDecrement() {
    val program = """
    var a = 0;
    --a;
    --a;
    setTestOutput(a);
    """

    interpret(program)

    assertEquals(LoxNumber(-2.0), getTestOutput())
  }

  @Test
  fun testPrefixIncrementForLoop() {
    val program =
        """
                var sum = 0;
                for (var i = 0; i < 20; ++i) {
                  sum = sum + i;
                }
                setTestOutput(sum);
            """
            .trimIndent()
    interpret(program)

    assertEquals(LoxNumber(190.0), getTestOutput())
  }

  private fun getTestOutput(): LoxObject {
    return testOutput.output
  }
  private fun interpret(code: String) {
    val tree = Parser((Scanner(code).scanTokens())).parse()
    assertEquals(0, Resolver().resolve(tree).size)
    interpreter.interpret(tree)
  }

  private fun eval(code: String): LoxObject {
    interpret("setTestOutput($code);")
    return testOutput.output
  }
}
