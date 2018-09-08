package net.raboof.parsekt

import kotlin.collections.arrayListOf
import kotlin.collections.joinToString
import kotlin.collections.plus
import kotlin.text.Regex

/** Extends the basic combinators with many character-specific parsers */
abstract class CharParsers<TInput> : Parsers<TInput>() {
    // implement anyChar to read a character from a sequence
    abstract val anyChar: Parser<TInput, Char>

    fun char(ch: Char): Parser<TInput, Char> {
        return anyChar.filter { c -> c == ch }.withErrorLabel("char($ch)")
    }

    fun char(predicate: (Char) -> Boolean): Parser<TInput, Char> {
        return anyChar.filter(predicate).withErrorLabel("char(predicate)")
    }

    fun char(regex: Regex): Parser<TInput, Char> {
        return anyChar.filter { ch: Char -> regex.matches(ch.toString()) }.withErrorLabel("char(/$regex/)")
    }

    //public val whitespace: Parser<TInput, List<Char>> = repeat(char(' ') or char('\t') or char('\n') or char('\r'));
    val whitespace = repeat(char(Regex("""\s"""))).withErrorLabel("whitespace")
    val wordChar = char(Regex("""\w"""))
    fun wsChar(ch: Char) = whitespace and char(ch)
    val token = repeat1(wordChar).between(whitespace)

    fun concat(p1: Parser<TInput, Char>, p2: Parser<TInput, List<Char>>): Parser<TInput, List<Char>> {
        return p1.project({v: Char, l: List<Char> -> arrayListOf(v) + l })({p2})
    }

    fun concat(vararg charParsers: Parser<TInput, Char>): Parser<TInput, List<Char>> {
        var parser : Parser<TInput, List<Char>> = succeed(emptyList())

        for (p in charParsers) {
            parser = parser.project({l: List<Char>, v: Char -> l + v })({p})
        }

        return parser
    }
    fun charPrefix(prefix: Char, parser: Parser<TInput, List<Char>>): Parser<TInput, List<Char>> {
        return concat(char(prefix), parser) or parser
    }

    /** greedy regex matcher */
    fun substring(regex: Regex): Parser<TInput, List<Char>> {
        return Parser { input ->
            var result = anyChar(input)
            when (result) {
                is Result.ParseError -> Result.ParseError(result)
                is Result.Value -> {
                    val temp = StringBuilder()
                    var lastRest: TInput = result.rest
                    var everMatched = false

                    while (result !is Result.ParseError) {
                        result as Result.Value

                        temp.append(result.value)
                        if (regex.matches(temp)) {
                            everMatched = true
                        } else if (everMatched) {
                            temp.deleteCharAt(temp.length-1)
                            break
                        }

                        lastRest = result.rest
                        result = anyChar(result.rest)
                    }

                    if (everMatched) {
                        Result.Value(temp.toList(), lastRest)
                    } else {
                        Result.ParseError("/$regex/", lastRest)
                    }
                }
            }
        }
    }

}

fun <TInput> Parser<TInput, List<Char>>.string(): Parser<TInput, String> {
    return this.mapResult { Result.Value(it.value.joinToString(""), it.rest) }
}