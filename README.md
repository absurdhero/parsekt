# ParSekt &#127863;

Parser Combinator library for [Kotlin](http://kotlinlang.org) based on
[Luke Hoban's blog post](http://blogs.msdn.com/b/lukeh/archive/2007/08/19/monadic-parser-combinators-using-c-3-0.aspx)
and the [Parsec Paper](http://research.microsoft.com/apps/pubs/default.aspx?id=65201).

Included are two sample parsers. The [prefix calculator](src/main/kotlin/net/raboof/parsekt/samples/PrefixCalc.kt) is a good place to start.
The guts of the parser live in the Parser, Parsers, and CharParsers classes.

This library is primarily built to demonstrate
Kotlin's type system and features with a minimum of code.
Its error reporting is incomplete and it is missing several convenience features.
For a more mature Kotlin/Java alternative for production use, look at
[JParsec](https://github.com/jparsec/jparsec) or contribute to this project.
I translated the JParsec Tutorial calculator into Kotlin
on [my blog](http://tumblr.raboof.net/post/135542198863/jparsec-tutorial-in-kotlin)
to get you started with JParsec.

## Concepts

For those who are not knee-deep in functional programming, the terminology
alone can be confusing. However, the concepts are simple and the library is not
terribly difficult to use or understand. Here is a small overview.
Consult the many links above for even more information.

A parser combinator works by providing the programmer with a set of
functions that take an input and return a result.
For example, a parser could read characters and output tokens,
or read tokens and output an AST, or read characters and output
just a single number. Any combination of input or output is possible.
Most of the library code does not care.
As a consequence, this means you can write both one and two pass parsers
for a compiler. DSLs are also a good fit for this type of parser.

The magical thing about a parser *combinator* library is that a small
list of primitive parser functions can be composed together by other functions
that describe how parsers should be combined. These functions are
called parser combinators.

Some of the most basic combinators are `repeat()` and `repeat1()`
which are equivalent to `*` and `+` in regex and BNF. The library also provides
other useful combinators like `or()` which will return the result of whichever
parser matched and more specialized ones like `concat()` which takes the
characters matched from two parsers and combines them into a single character
string.

## To Do

These are some improvements that I think would turn this into a generally
useful parser library.

- track character numbers in error reporting
- for character inputs, make it possible to track line numbers
- infix calculator example with generic infix parsers
- find a more readable way to express monadic operations (Parser::mapJoin could be nicer)
- optimize "or" combinator
