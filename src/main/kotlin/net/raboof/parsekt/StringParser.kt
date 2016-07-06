package net.raboof.parsekt

import kotlin.text.substring

/** The class of parsers that takes a String as input */
open class StringParser() : CharParsers<String>() {
    override val anyChar: Parser<String, Char>
        get() = Parser { input: String ->
            when (input.length) {
                0 -> Consumable.Empty(Result.ParseError<String, Char>("EOF", null, ""))
                1 -> Consumable.Consumed(Result.Value(input[0], ""))
                else -> Consumable.Consumed(Result.Value(input[0], input.substring(1)))
            }
        }
}

