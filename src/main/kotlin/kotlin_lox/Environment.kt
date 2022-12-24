package kotlin_lox

class Environment(private val enclosing: Environment? = null) {
  private val values = mutableMapOf<String, Literal>()

  fun get(key: String): Literal? {
    // Attempt to get key from environment, otherwise recursively from enclosing.
    return values[key] ?: enclosing?.get(key)
  }

  fun define(key: String, value: Literal) {
    values[key] = value
  }

  // Assign a value to a defined variable in the first enclosing scope.
  // Return true if successful, false if the variable hasn't been defined.
  fun assign(key: String, value: Literal): Boolean {
    if (key !in values) {
      return enclosing?.assign(key, value) ?: false
    }
    values[key] = value
    return true
  }
}
