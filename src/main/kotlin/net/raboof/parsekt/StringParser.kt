package net.raboof.parsekt

import kotlin.text.substring

open public class StringParser() : CharParsers<String>() {
    override val anyChar: Parser<String, Char>
        get() = Parser { input: String ->
            when (input.length) {
                0 -> null
                1 -> Result(input[0], "")
                else -> Result(input[0], input.substring(1))
            }
        }
}

