package kotlin_lox

import java.lang.System.currentTimeMillis

object Clock : LoxCallable {
    override fun arity(): Int {
        return 0;
    }

    override fun call(interpreter: Interpreter, arguments: List<Expr>): LoxObject {
        return LoxNumber(currentTimeMillis() / 1000.0)
    }

    override fun toString(): String {
        return "<fn clock>"
    }
}
