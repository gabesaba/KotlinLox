package kotlin_lox

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin_lox.ResolverError.*

class ResolverTest {

  @Test
  fun testEmptyProgram() {
    val program = ""

    val errors = program.resolve()

    assertEquals(listOf(), errors)
  }

  @Test
  fun testVariableDefinedTwice() {
    val program = """
           var a;
           var a;
        """

    val errors = program.resolve()

    assertContains(errors, VariableDefinedTwice)
  }

  @Test
  fun testVariableDefinedTwiceInBlock() {
    val program = """
           {
             var a;
             var a;
           }
        """

    val errors = program.resolve()

    assertContains(errors, VariableDefinedTwice)
  }

  @Test
  fun variableNeverSet() {
    val program = """
        {
          var a;
        }
        """

    val errors = program.resolve()

    assertContains(errors, VariableNeverSet)
  }

  @Test
  fun variableNeverRead() {
    val program = """
        {
          var a = 5;
        }
        """

    val errors = program.resolve()

    assertContains(errors, VariableNeverRead)
  }

  @Test
  fun functionNeverCalled() {
    val program = """
            {
              fun hello() {}
            }
        """

    val errors = program.resolve()

    assertContains(errors, VariableNeverRead)
  }

  @Test
  fun variableDefinedAfterDeclaration() {
    val program = """
        var a;
        a = 5;
        a;
        """

    val errors = program.resolve()

    assertEquals(listOf(), errors)
  }

  @Test
  fun variableDefinedAfterDeclarationInBlock() {
    val program = """
        var a;
        {
          a = 5;
        }
        a;
        """

    val errors = program.resolve()

    assertEquals(listOf(), errors)
  }

  @Test
  fun returnInvalidOutsideOfFunction() {
    val program = """
            return;
        """

    val errors = program.resolve()

    assertEquals(listOf(ReturnOutsideOfFunction), errors)
  }

  @Test
  fun returnValidInsideFunction() {
    val program =
        """
            fun hello() {
              return;
            }
            hello();
        """

    val errors = program.resolve()

    assertEquals(listOf(), errors)
  }

  @Test
  fun testForLoop() {
    val program = """
            for (var i = 0; i < 10; i = i + 1) {}
        """

    val errors = program.resolve()

    assertEquals(listOf(), errors)
  }

  @Test
  fun testReassignAfterRead() {
    val program =
        """
            {
            var a = 5;
            a;
            a = 6;
            }
        """

    val errors = program.resolve()

    assertEquals(listOf(), errors)
  }

  private fun String.resolve(): List<ResolverError> {
    val tokens = Scanner(this).scanTokens()
    val tree = Parser(tokens).parse()
    val resolver = Resolver()
    return resolver.resolve(tree)
  }
}
