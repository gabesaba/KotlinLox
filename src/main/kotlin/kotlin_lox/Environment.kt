package kotlin_lox

class Environment {
  private val values = mutableMapOf<String, Literal>()

  fun get(key: String): Literal? {
    return values[key]
  }

  fun define(key: String, value: Literal) {
    values[key] = value
  }
}
