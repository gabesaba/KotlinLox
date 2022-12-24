package kotlin_lox

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EnvironmentTest {

  @Test
  fun testMissingKey() {
    assertNull(Environment().get("missing"))
  }

  @Test
  fun testPresentKey() {
    val env = Environment()
    env.define("key", LoxString("hello"))
    assertEquals(env.get("key"), LoxString("hello"))
  }

  @Test
  fun testReturnFalseWhenAssigningUndefined() {
    val env = Environment()
    assertFalse { env.assign("key", LoxString("hello")) }
  }

  @Test
  fun testReturnTrueWhenAssigningUndefined() {
    val env = Environment()

    env.define("key", LoxNil)

    assertTrue { env.assign("key", LoxString("hello")) }
  }

  @Test
  fun testMissingKeyInEnclosing() {
    val enclosing = Environment()
    val env = Environment(enclosing)

    assertNull(env.get("missing"))
  }

  @Test
  fun testMissingKeyFoundInEnclosing() {
    val enclosing = Environment()
    val env = Environment(enclosing)

    enclosing.define("found", LoxNumber(3.14))

    assertEquals(env.get("found"), LoxNumber(3.14))
  }

  @Test
  fun testShadowedKey() {
    val enclosing = Environment()
    val env = Environment(enclosing)

    enclosing.define("key", LoxNumber(3.14))
    env.define("key", LoxNumber(6.28))

    assertEquals(env.get("key"), LoxNumber(6.28))
    assertEquals(enclosing.get("key"), LoxNumber(3.14))
  }

  @Test
  fun testAssignToEnclosing() {
    val enclosing = Environment()
    val env = Environment(enclosing)

    enclosing.define("key", LoxNumber(3.14))
    env.assign("key", LoxNumber(6.28))

    assertEquals(env.get("key"), LoxNumber(6.28))
    assertEquals(enclosing.get("key"), LoxNumber(6.28))
  }
}
