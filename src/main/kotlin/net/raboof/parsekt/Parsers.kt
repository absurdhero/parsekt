package net.raboof.parsekt

import kotlin.collections.arrayListOf
import kotlin.collections.emptyList
import kotlin.collections.plus

/** Base parser combinator class which contains the core combinators */
abstract class Parsers<TInput> {
    fun <TValue> succeed(value: TValue): Parser<TInput, TValue> {
        return Parser { input -> Result.Value(value, input) }
    }

    fun <TValue> repeat(parser: Parser<TInput, TValue>): Parser<TInput, List<TValue>> {
        return repeat1(parser) or succeed(emptyList())
    }

    fun <TValue> repeat1(parser: Parser<TInput, TValue>): Parser<TInput, List<TValue>> {
        return parser.mapJoin({ repeat(parser) }, { v: TValue, l: List<TValue> -> arrayListOf(v) + l })
                .wrapError("repeat1")
    }
}
