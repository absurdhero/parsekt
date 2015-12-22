package net.raboof.parsekt

// based on http://blogs.msdn.com/b/lukeh/archive/2007/08/19/monadic-parser-combinators-using-c-3-0.aspx

open class Parser<TInput, TValue>(val f: (TInput) -> Result<TInput, TValue>?) {
    operator fun invoke(input: TInput): Result<TInput, TValue>? = f(input)

    infix fun or(other: Parser<TInput, TValue>): Parser<TInput, TValue> {
        return Parser({ input -> this(input) ?: other(input) })
    }

    infix fun and(other: Parser<TInput, TValue>): Parser<TInput, TValue> {
        return Parser({ input ->
            val result = this(input)
            if (result == null) {
                null
            } else {
                other(result.rest)
            }
        })
    }

    // like and() but ignores the both the type and value of the first parser
    infix fun <TValue2> then(other: Parser<TInput, TValue2>): Parser<TInput, TValue2> {
        return Parser({ input ->
            val result = this(input)
            if (result == null) {
                null
            } else {
                other(result.rest)
            }
        })
    }

    // like then but returns the value of the first parser
    infix fun before(other: Parser<TInput, *>): Parser<TInput, TValue> {
        return Parser({ input ->
            val result = this(input)
            if (result == null) {
                null
            } else {
                other(result.rest)
                result
            }
        })
    }

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

    fun <TValue2> map(selector: (TValue) -> TValue2): Parser<TInput, TValue2> {
        return Parser({ input ->
            val result = this(input)
            if (result == null) {
                null
            } else {
                Result(selector(result.value), result.rest)
            }
        })
    }

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

    fun <TIntermediate, TValue2> project(projector: (TValue, TIntermediate) -> TValue2)
            : ((TValue) -> Parser<TInput, TIntermediate>) -> Parser<TInput, TValue2> {
        return { selector: (TValue) -> Parser<TInput, TIntermediate> ->
            Parser({ input ->
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
    }

    fun <TValue2> withResult(selector: (Result<TInput, TValue>) -> Result<TInput, TValue2>?): Parser<TInput, TValue2> {
        return Parser({ input ->
            val result = this(input)
            if (result == null) {
                null
            } else {
                selector(result)
            }
        })
    }

    fun between(start: Parser<TInput, *>, end: Parser<TInput, *> = start): Parser<TInput, TValue> {
        return start.then(this).withResult { res ->
            end.withResult { Result(res.value, it.rest) }.invoke(res.rest)
        }
    }

    fun asList() : Parser<TInput, List<TValue>> {
        return withResult { Result(listOf(it.value), it.rest) }
    }
}