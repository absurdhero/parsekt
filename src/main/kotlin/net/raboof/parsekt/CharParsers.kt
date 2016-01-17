package net.raboof.parsekt

import kotlin.collections.arrayListOf
import kotlin.collections.joinToString
import kotlin.collections.plus
import kotlin.text.Regex

/** Extends the basic combinators with many character-specific parsers */
abstract class CharParsers<TInput>() : Parsers<TInput>() {
    // implement anyChar to read a character from a sequence
    abstract val anyChar: Parser<TInput, Char>

    public fun char(ch: Char): Parser<TInput, Char> {
        return anyChar.filter({ c -> c == ch }).withErrorLabel("char($ch)")
    }

    public fun char(predicate: (Char) -> Boolean): Parser<TInput, Char> {
        return anyChar.filter(predicate).withErrorLabel("char(predicate)")
    }

    public fun char(regex: Regex): Parser<TInput, Char> {
        return anyChar.filter({ ch: Char -> regex.matches(ch.toString()) }).withErrorLabel("char(/$regex/)")
    }

//    public val whitespace: Parser<TInput, List<Char>> = repeat(char(' ') or char('\t') or char('\n') or char('\r'));
    public val whitespace = repeat(char(Regex("""\s"""))).withErrorLabel("whitespace")
    public val wordChar = char(Regex("""\w"""))
    public fun wsChar(ch: Char) = whitespace and char(ch)
    public val token = repeat1(wordChar).between(whitespace)

    public fun concat(p1: Parser<TInput, Char>, p2: Parser<TInput, List<Char>>): Parser<TInput, List<Char>> {
        return p1.project({v: Char, l: List<Char> -> arrayListOf(v) + l })({p2})
    }

    public fun charPrefix(prefix: Char, parser: Parser<TInput, List<Char>>): Parser<TInput, List<Char>> {
        return concat(char(prefix), parser) or parser
    }

    /** greedy regex matcher */
    public fun substring(regex: Regex): Parser<TInput, List<Char>> {
        return Parser({ input ->
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
                        } else if (everMatched == true) {
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
        })
    }

}

fun <TInput> Parser<TInput, List<Char>>.string(): Parser<TInput, String> {
    return this.mapResult { Result.Value(it.value.joinToString(""), it.rest) }
}