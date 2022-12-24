package kotlin_lox

class Environment {
  private val values = mutableMapOf<String, Literal>()

  fun get(key: String): Literal? {
    return values[key]
  }

  fun define(key: String, value: Literal) {
    values[key] = value
  }

  // Assign a value to a defined variable.
  // Return true if successful, false if the variable hasn't been defined.
  fun assign(key: String, value: Literal): Boolean {
    if (key !in values) {
      return false
    }
    values[key] = value
    return true
  }
}
