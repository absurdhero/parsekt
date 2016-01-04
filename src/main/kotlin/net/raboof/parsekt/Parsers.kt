package net.raboof.parsekt

import kotlin.collections.arrayListOf
import kotlin.collections.emptyList
import kotlin.collections.plus

/** Base parser combinator class which contains the core combinators */
abstract class Parsers<TInput> {
    public fun <TValue> succeed(value: TValue): Parser<TInput, TValue> {
        return Parser({ input -> Result(value, input) })
    }

    public fun <TValue> repeat(parser: Parser<TInput, TValue>): Parser<TInput, List<TValue>> {
        return repeat1(parser) or succeed<List<TValue>>(emptyList())
    }

    public fun <TValue> repeat1(parser: Parser<TInput, TValue>): Parser<TInput, List<TValue>> {
        return parser.mapJoin({ v -> repeat(parser) }, { v: TValue, l: List<TValue> -> arrayListOf(v) + l })
    }
}
