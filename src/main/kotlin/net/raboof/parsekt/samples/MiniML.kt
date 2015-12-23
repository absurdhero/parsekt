package net.raboof.parsekt.samples

import net.raboof.parsekt.CharParsers
import net.raboof.parsekt.Parser
import net.raboof.parsekt.Reference
import net.raboof.parsekt.string
import kotlin.text.isLetter
import kotlin.text.isLetterOrDigit

// Term and its derived classes define the AST for terms in the MiniML language.
public interface Terminal { }
public data class LambdaTerm(val ident: String, val term: Terminal) : Terminal {}
public data class LetTerm(val ident: String, val rhs: Terminal, val body: Terminal) : Terminal {}
public data class AppTerm(val func: Terminal, val args: List<Terminal>) : Terminal {}
public data class VarTerm(val ident: String) : Terminal {}

/* Translated from MiniML on Luke Hoban's Blog
   http://blogs.msdn.com/b/lukeh/archive/2007/08/19/monadic-parser-combinators-using-c-3-0.aspx?PageIndex=2#comments
 */
abstract class MiniML<TInput>(): CharParsers<TInput>() {

    val Id = whitespace then concat(char(Char::isLetter), repeat(char(Char::isLetterOrDigit))).string()
    val Ident = Id.filter { it != "let" && it != "in" }
    val LetId = Id.filter { it == "let" }
    val InId = Id.filter { it == "in" }

    /*
     from u1 in WsChr('\\')
     from x in Ident
     from u2 in WsChr('.')
     from t in Term
     select (Term)new LambdaTerm(x,t)
     */
    val Lambda: Parser<TInput, Terminal> = Ident.between(wsChar('\\'), wsChar('.'))
            .mapJoin({ Term }, { x, t -> LambdaTerm(x,t)})

    /*
     from letid in LetId
     from x in Ident // capture
     from u1 in WsChr('=')
     from t in Term // capture
     from inid in InId
     from c in Term // capture
     select (Term)new LetTerm(x,t,c))
    */
    val Let : Parser<TInput, Terminal> = Ident.between(LetId, wsChar('='))
            .mapJoin(
                    {(Term before InId).mapJoin({Term}, {v, s -> Pair(v,s)})},
                    { v, s -> LetTerm(v, s.first, s.second)})

    /*
     from t in Term1
     from ts in Rep(Term1)
     select (Term)new AppTerm(t,ts))
     */

    private val Term1Ref : Reference<TInput, Terminal> = Reference()
    val Term1 = Term1Ref.get()

    val App : Parser<TInput, Terminal> = Term1.mapJoin({repeat(Term1)}, {t, ts -> AppTerm(t, ts)})

    val Term = Lambda or Let or App

    /*
     Term1 = (from x in Ident
                 select (Term)new VarTerm(x))
                .OR(
                (from u1 in WsChr('(')
                 from t in Term
                 from u2 in WsChr(')')
                 select t));
     */
    init { Term1Ref.set(Ident.map { VarTerm(it) as Terminal } or Term.between(char('('), char(')'))) }

    val All : Parser<TInput, Terminal> = Term before wsChar(';') //Term.mapJoin({wsChar(';')}, {v, i -> v});
}

