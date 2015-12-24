ParSekt &#127863;
=======

Parser Combinator library for [Kotlin](http://kotlinlang.org) based on
[Luke Hoban's blog post](http://blogs.msdn.com/b/lukeh/archive/2007/08/19/monadic-parser-combinators-using-c-3-0.aspx)
and the [Parsec Paper](http://research.microsoft.com/apps/pubs/default.aspx?id=65201).

This library is currently a toy implementation that demonstrates
Kotlin's type system and features.
It has no error reporting and is missing several convenience features.
For a viable alternative, look at
[JParsec](https://github.com/jparsec/jparsec) or contribute to this project.
I translated the JParsec Tutorial calculator into Kotlin
on [my blog](http://tumblr.raboof.net/post/135542198863/jparsec-tutorial-in-kotlin)
to get you started.


To Do
-----

- error reporting
- write calculator example
- find a more readable way to express monadic operations (Parser::mapJoin)
- optimize "or" combinator
