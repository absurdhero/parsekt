ParSekt &#127863;
=======

Parser Combinator library for [Kotlin](http://kotlinlang.org) based on
[Luke Hoban's blog post](http://blogs.msdn.com/b/lukeh/archive/2007/08/19/monadic-parser-combinators-using-c-3-0.aspx)
and the [Parsec Paper](http://research.microsoft.com/apps/pubs/default.aspx?id=65201).

Included are two sample parsers. The [prefix calculator](src/main/kotlin/net/raboof/parsekt/samples/PrefixCalc.kt) is a good place to start.
The guts of the parser live in the Parser, Parsers, and CharParsers classes.

This library is currently a toy implementation that demonstrates
Kotlin's type system and features with a minimum of code.
It has no error reporting and is missing several convenience features.
For a viable Kotlin/Java alternative, look at
[JParsec](https://github.com/jparsec/jparsec) or contribute to this project.
I translated the JParsec Tutorial calculator into Kotlin
on [my blog](http://tumblr.raboof.net/post/135542198863/jparsec-tutorial-in-kotlin)
to get you started.


To Do
-----

These are some improvements that I think would turn this into a generally useful parser library.

- error reporting
- infix calculator example with generic infix parsers
- find a more readable way to express monadic operations (Parser::mapJoin)
- optimize "or" combinator
