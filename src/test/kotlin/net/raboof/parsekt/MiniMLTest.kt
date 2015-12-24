package net.raboof.parsekt

import net.raboof.parsekt.samples.*
import org.junit.Test
import kotlin.collections.emptyList
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.text.substring


class MiniMLTest {
    public class MiniMLStringParser() : MiniML<String>() {
        override val anyChar: Parser<String, Char>
            get() = Parser { input: String ->
                when (input.length) {
                    0 -> null
                    1 -> Result(input[0], "")
                    else -> Result(input[0], input.substring(1))
                }
            }
    }

    val parser = MiniMLStringParser();

    @Test
    public fun ident() {
        assertEquals("A123", parser.Ident("""A123""")?.value)
    }

    @Test
    public fun lambda() {
        assertEquals(
                LambdaTerm("x", LambdaTerm("y", AppTerm(VarTerm("z")))),
                parser.Lambda("""\x.\y.z""")?.value)
    }

    @Test
    public fun term1() {
        assertEquals(VarTerm("A123"), parser.Term1("""A123""")?.value)
        assertEquals(AppTerm(VarTerm("x")), parser.Term1("""(x)""")?.value)
    }

    @Test
    public fun term() {
        // lambda
        assertEquals(
                LambdaTerm("x", LambdaTerm("y", AppTerm(VarTerm("z")))),
                parser.Term("""\x.\y.z""")?.value)
        // app
        assertEquals((AppTerm(VarTerm("A123"))), parser.Term("""A123""")?.value)
    }

    @Test
    public fun let() {
        assertEquals(
                LetTerm("x", AppTerm(VarTerm("y")), AppTerm(VarTerm("z"))) as Terminal,
                parser.Let("""let x = y in z""")?.value)
    }

    @Test
    public fun program() {
        assertNotNull(parser.All("\\x.y;"))
        assertNull(parser.All("\\x.y"), "do not match if semicolon missing")

        assertNotNull(parser.All("""
             let true = \x.\y.x in
             let false = \x.\y.y in
             let if = \b.\l.\r.(b l) r in
             if true then false else true;"""))
    }
}