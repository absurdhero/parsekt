package net.raboof.parsekt.samples

import net.raboof.parsekt.CharParsers
import net.raboof.parsekt.Parser
import net.raboof.parsekt.Reference
import net.raboof.parsekt.string
import kotlin.collections.emptyList
import kotlin.text.isLetter
import kotlin.text.isLetterOrDigit

/* Translated from MiniML on Luke Hoban's Blog
   http://blogs.msdn.com/b/lukeh/archive/2007/08/19/monadic-parser-combinators-using-c-3-0.aspx?PageIndex=2#comments
 */

// AST for the MiniML language
interface Terminal { }
data class LambdaTerm(val ident: String, val term: Terminal) : Terminal {}
data class LetTerm(val ident: String, val rhs: Terminal, val body: Terminal) : Terminal {}
data class AppTerm(val func: Terminal, val args: List<Terminal> = emptyList()) : Terminal {}
data class VarTerm(val ident: String) : Terminal {}

abstract class MiniML<TInput>(): CharParsers<TInput>() {

    val Id = whitespace and concat(char(Char::isLetter), repeat(char(Char::isLetterOrDigit))).string()
    val Ident = Id.filter { it != "let" && it != "in" }
    val LetId = Id.filter { it == "let" }
    val InId = Id.filter { it == "in" }

    val Lambda: Parser<TInput, Terminal> = Ident.between(wsChar('\\'), wsChar('.'))
            .mapJoin({ Term }, { x, t -> LambdaTerm(x,t)})

    val Let : Parser<TInput, Terminal> = Ident.between(LetId, wsChar('='))
            .mapJoin(
                    {(Term before InId).mapJoin({Term}, {v, s -> Pair(v,s)})},
                    { v, s -> LetTerm(v, s.first, s.second)})

    private val Term1Ref : Reference<TInput, Terminal> = Reference()
    val Term1 = Term1Ref.get()

    val App : Parser<TInput, Terminal> = Term1.mapJoin({repeat(Term1)}, {t, ts -> AppTerm(t, ts)})

    val Term = Lambda or Let or App

    init { Term1Ref.set(Ident.map { VarTerm(it) as Terminal } or Term.between(char('('), char(')'))) }

    val All : Parser<TInput, Terminal> = Term before wsChar(';')
}

