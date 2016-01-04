package net.raboof.parsekt

import kotlin.collections.listOf

// based on http://blogs.msdn.com/b/lukeh/archive/2007/08/19/monadic-parser-combinators-using-c-3-0.aspx

/** A Parser is both a function and an object with methods that return derivative parsers */
open class Parser<TInput, TValue>(val f: (TInput) -> Result<TInput, TValue>?) {

    /** A parser can be invoked as a function of an input that returns a result or null */
    operator fun invoke(input: TInput): Result<TInput, TValue>? = f(input)

    /* the following filter and map functions are the building blocks used to derive new parsers */

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

    /** This function is a convenient way to build parsers that act on more that one input parser.
     *
     * It invokes "this" followed by the parser returned from the selector function.
     * It then passes the two resulting values to the projector which returns one result.
     */
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

    /* These are some essential combinators which are
       functions that take parsers as arguments and return a new parser
     */

    infix fun or(other: Parser<TInput, TValue>): Parser<TInput, TValue> {
        return Parser({ input -> this(input) ?: other(input) })
    }

    infix fun <TValue2> and(other: Parser<TInput, TValue2>): Parser<TInput, TValue2> =
            this.mapJoin({ other }, { v, i -> i })

    // like "and" but returns the value of the first parser
    infix fun <TValue2> before(other: Parser<TInput, TValue2>): Parser<TInput, TValue> =
            this.mapJoin({ other }, { v, i -> v })


    /* Generally useful functions */

    // curry the projector function in mapJoin
    fun <TIntermediate, TValue2> project(projector: (TValue, TIntermediate) -> TValue2)
            : ((TValue) -> Parser<TInput, TIntermediate>) -> Parser<TInput, TValue2> {
        return { selector: (TValue) -> Parser<TInput, TIntermediate> ->
            mapJoin(selector, projector)
        }
    }

    // extract the result of this parser from the input between two other parsers
    fun between(start: Parser<TInput, *>, end: Parser<TInput, *> = start): Parser<TInput, TValue> {
        return start and this before end
    }

    fun asList(): Parser<TInput, List<TValue>> {
        return mapResult { Result(listOf(it.value), it.rest) }
    }
}