package net.raboof.parsekt

import kotlin.collections.listOf

// based on http://blogs.msdn.com/b/lukeh/archive/2007/08/19/monadic-parser-combinators-using-c-3-0.aspx

open class Parser<TInput, TValue>(val f: (TInput) -> Result<TInput, TValue>?) {
    operator fun invoke(input: TInput): Result<TInput, TValue>? = f(input)

    fun filter(pred: (TValue) -> Boolean): Parser<TInput, TValue> {
        return Parser({ input ->
            val result = this(input)
            if (result == null || !pred(result.value)) {
                null
            } else {
                result
            }
        })
    }

    fun <TValue2> mapResult(selector: (Result<TInput, TValue>) -> Result<TInput, TValue2>?): Parser<TInput, TValue2> {
        return Parser({ input ->
            val result = this(input)
            if (result == null) {
                null
            } else {
                selector(result)
            }
        })
    }

    fun <TValue2> map(selector: (TValue) -> TValue2): Parser<TInput, TValue2>
            = mapResult { result -> Result(selector(result.value), result.rest) }

    fun <TIntermediate, TValue2> mapJoin(
            selector: (TValue) -> Parser<TInput, TIntermediate>,
            projector: (TValue, TIntermediate) -> TValue2
    ): Parser<TInput, TValue2> {
        return Parser({ input ->
            val res = this(input)
            if (res == null) {
                null
            } else {
                val v = res.value
                val res2 = selector(v)(res.rest)
                if (res2 == null) null
                else Result(projector(v, res2.value), res2.rest)
            }
        })
    }

    infix fun or(other: Parser<TInput, TValue>): Parser<TInput, TValue> {
        return Parser({ input -> this(input) ?: other(input) })
    }

    infix fun <TValue2> and(other: Parser<TInput, TValue2>): Parser<TInput, TValue2> =
            this.mapJoin({ other }, { v, i -> i })

    // like "and" but returns the value of the first parser
    infix fun <TValue2> before(other: Parser<TInput, TValue2>): Parser<TInput, TValue> =
            this.mapJoin({ other }, { v, i -> v })


    // curry the projector function in mapJoin
    fun <TIntermediate, TValue2> project(projector: (TValue, TIntermediate) -> TValue2)
            : ((TValue) -> Parser<TInput, TIntermediate>) -> Parser<TInput, TValue2> {
        return { selector: (TValue) -> Parser<TInput, TIntermediate> ->
            mapJoin(selector, projector)
        }
    }

    // extract the result of this parser from the input between two other parsers
    fun between(start: Parser<TInput, *>, end: Parser<TInput, *> = start): Parser<TInput, TValue> {
        return start.and(this).mapResult { res ->
            end.mapResult { Result(res.value, it.rest) }.invoke(res.rest)
        }
    }

    fun asList() : Parser<TInput, List<TValue>> {
        return mapResult { Result(listOf(it.value), it.rest) }
    }
}