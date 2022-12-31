package kotlin_lox

class Environment(private val enclosing: Environment? = null) {
  private val values = mutableMapOf<String, LoxObject>()
  private val defined = mutableSetOf<String>()

  fun get(key: String): LoxObject? {
    // Attempt to get key from environment, otherwise recursively from enclosing.
    return values[key] ?: enclosing?.get(key)
  }

  fun define(key: String, value: LoxObject) {
    defined.add(key)
    assign(key, value)
  }

  fun define(key: String) {
    defined.add(key)
  }

  fun split(): Environment {
    val newEnvironment = Environment(enclosing)
    newEnvironment.values.putAll(values)
    newEnvironment.defined.addAll(defined)
    return newEnvironment
  }

  // Assign a value to a defined variable in the first enclosing scope.
  // Return true if successful, false if the variable hasn't been defined.
  fun assign(key: String, value: LoxObject): Boolean {
    if (key !in values && key !in defined) {
      return enclosing?.assign(key, value) ?: false
    }
    values[key] = value
    return true
  }
}
