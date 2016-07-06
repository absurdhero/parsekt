package net.raboof.parsekt

// based on http://blogs.msdn.com/b/lukeh/archive/2007/08/19/monadic-parser-combinators-using-c-3-0.aspx

/** A Parser is both a function and an object with methods that return derivative parsers */
open class Parser<TInput, TValue>(val f: (TInput) -> Consumable<TInput, TValue>) {

    /** A parser can be invoked as a function of an input that returns a result */
    operator fun invoke(input: TInput): Consumable<TInput, TValue> = f(input)

    /* the following filter and map functions are the building blocks used to derive new parsers */

    fun filter(pred: (TValue) -> Boolean): Parser<TInput, TValue> {
        return Parser({ input ->
            val consumable = this(input)
            when (consumable) {
                is Consumable.Empty -> consumable
                is Consumable.Consumed -> {
                    val result = consumable.result
                    when (result) {
                        is Result.Value -> if (pred(result.value)) {
                            consumable
                        } else {
                            Consumable.Empty(Result.ParseError("filter", null, input))
                        }
                        is Result.ParseError -> consumable.withResult(result)
                    }
                }
            }
        })
    }

    fun <TValue2> mapResult(selector: (Result.Value<TInput, TValue>) -> Result<TInput, TValue2>): Parser<TInput, TValue2> {
        return Parser({ input ->
            val consumable = this(input)
            val result = consumable.result
            when (result) {
                is Result.Value -> consumable.withResult(selector(result))
                is Result.ParseError -> consumable.withResult(Result.ParseError(result))
            }
        })
    }

    fun <TValue2> map(selector: (TValue) -> TValue2): Parser<TInput, TValue2>
            = mapResult { result -> Result.Value(selector(result.value), result.rest) }

    /** This function is a convenient way to build parsers that act on more that one input parser.
     *
     * It invokes "this" followed by the parser returned from the selector function.
     * It then passes the two resulting values to the projector which returns one result.
     *
     * The selector "maps" the value from "this" to an intermediate parser.
     * Then the projector "joins" the original value and the mapped value into a new value.
     *
     * See usages of this function in this library for examples of how to make use of it.
     */
    fun <TIntermediate, TValue2> mapJoin(
            selector: (TValue) -> Parser<TInput, TIntermediate>,
            projector: (TValue, TIntermediate) -> TValue2
    ): Parser<TInput, TValue2> {
        return Parser({ input ->
            val consumed1 = this(input)
            val res1 = consumed1.result
            when (res1) {
                is Result.ParseError -> consumed1.withResult(Result.ParseError(res1))
                is Result.Value -> {
                    val v = res1.value
                    val consumed2 = selector(v)(res1.rest)

                    val createConsumer : (result: Result<TInput, TValue2>) -> Consumable<TInput, TValue2>
                    if (consumed1 is Consumable.Empty && consumed2 is Consumable.Empty) {
                        createConsumer = Consumable.factory(false)
                    } else {
                        createConsumer = Consumable.factory(true)
                    }

                    val res2 = consumed2.result
                    when (res2) {
                        is Result.ParseError -> createConsumer(Result.ParseError<TInput, TValue2>(res2))
                        is Result.Value -> createConsumer(Result.Value(projector(v, res2.value), res2.rest))
                    }
                }
            }
        })
    }

    /* These are some essential combinators which are
       functions that take parsers as arguments and return a new parser
     */

    infix fun or(other: Parser<TInput, TValue>): Parser<TInput, TValue> {
        return Parser({ input ->
            val consumable = this(input)
            val result = consumable.result
            when (result) {
                is Result.Value -> consumable.withResult(result)
                is Result.ParseError -> other(input)
            }
        })
    }

    infix fun <TValue2> and(other: Parser<TInput, TValue2>): Parser<TInput, TValue2> =
            this.mapJoin({ other }, { v, i -> i })

    // like "and" but returns the value of the first parser
    infix fun <TValue2> before(other: Parser<TInput, TValue2>): Parser<TInput, TValue> =
            this.mapJoin({ other }, { v, i -> v })


    /* error tracking */

    /** Allows a reported error from a parser to be modified.
     *
     * This is useful when the combinator knows more about why an error happened.
     */
    fun mapError(errorFunc: (Result.ParseError<TInput, TValue>) -> Result.ParseError<TInput, TValue>): Parser<TInput, TValue> {
        return Parser({ input ->
            val consumable = this(input)
            val result = consumable.result
            when (result) {
                is Result.Value -> consumable.withResult(result)
                is Result.ParseError -> consumable.withResult(errorFunc(result))
            }
        })
    }

    fun withErrorLabel(label: String): Parser<TInput, TValue> {
        return mapError { Result.ParseError(label, it.child, it.rest) }
    }

    fun wrapError(label: String): Parser<TInput, TValue> {
        return mapError { Result.ParseError(label, it) }
    }

    /* Generally useful functions */

    /** curry the projector function in mapJoin
     *
     * @see mapJoin
     */
    fun <TIntermediate, TValue2> project(projector: (TValue, TIntermediate) -> TValue2)
            : ((TValue) -> Parser<TInput, TIntermediate>) -> Parser<TInput, TValue2> {
        return { selector: (TValue) -> Parser<TInput, TIntermediate> ->
            mapJoin(selector, projector)
        }
    }

    // extract the result of this parser from the input between two other parsers
    fun between(start: Parser<TInput, *>, end: Parser<TInput, *> = start): Parser<TInput, TValue> {
        return (start and this before end).wrapError("between")
    }

    fun asList(): Parser<TInput, List<TValue>> {
        return mapResult { Result.Value(listOf(it.value), it.rest) }
    }

    // sometimes useful for working around covariance problems (or from T to T?)
    fun <TValue2> cast(): Parser<TInput, TValue2> {
        @Suppress("CAST_NEVER_SUCCEEDS")
        return this as Parser<TInput, TValue2>
    }
}