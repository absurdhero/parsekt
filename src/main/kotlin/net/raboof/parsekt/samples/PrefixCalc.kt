package net.raboof.parsekt.samples

import net.raboof.parsekt.*
import kotlin.collections.map
import kotlin.collections.reduce
import kotlin.text.isDigit
import kotlin.text.toLong


/** A 4-function calculator for integers that uses prefix notation. */
abstract class PrefixCalc<TInput> : CharParsers<TInput>() {
    /** evaluate the input or return null */
    fun evaluate(input: TInput) : Long {
        return operation(input).valueOrFail().evaluate()
    }

    interface Expr {
        fun evaluate() : Long
    }

    data class Operation(val operator: Char, val exprs: List<Expr>) : Expr {
        override fun evaluate() : Long {
            val terms = exprs.map { it.evaluate() }
            return when (operator) {
                '+' -> terms.reduce {acc, next -> acc + next}
                '-' -> terms.reduce {acc, next -> acc - next}
                '*' -> terms.reduce {acc, next -> acc * next}
                '/' -> terms.reduce {acc, next -> acc / next}
                else -> throw IllegalArgumentException()
            }
        }
    }

    data class Number(val value: String) : Expr {
        override fun evaluate() : Long {
            return value.toLong()
        }
    }

    val exprRef: Reference<TInput, Expr> = Reference()
    val expr = exprRef.get()

    val number: Parser<TInput, Expr> = whitespace and charPrefix('-', repeat1(char(Char::isDigit))).string().map { Number(it) as Expr }
    val plusOp = prefixOp('+')
    val minusOp = prefixOp('-')
    val multiplyOp = prefixOp('*')
    val divideOp = prefixOp('/')

    val operation = plusOp or minusOp or multiplyOp or divideOp

    init { exprRef.set(number or operation.between(wsChar('('), wsChar(')'))) }

    private fun prefixOp(opChar: Char): Parser<TInput, Expr> = wsChar(opChar) and repeat1(expr).map { Operation(opChar, it) as Expr }
}