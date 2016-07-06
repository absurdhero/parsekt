package net.raboof.parsekt.samples

import net.raboof.parsekt.Consumable
import net.raboof.parsekt.Parser
import net.raboof.parsekt.Result
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.text.substring


class MiniMLTest {
    class MiniMLStringParser() : MiniML<String>() {
        override val anyChar: Parser<String, Char>
            get() = Parser { input: String ->
                when (input.length) {
                    0 -> Consumable.Empty(Result.ParseError<String, Char>("EOF", null, ""))
                    1 -> Consumable.Consumed(Result.Value(input[0], ""))
                    else -> Consumable.Consumed(Result.Value(input[0], input.substring(1)))
                }
            }
    }

    val parser = MiniMLStringParser();

    @Test fun ident() {
        assertEquals("A123", parser.Ident("""A123""").valueOrFail())
    }

    @Test fun lambda() {
        assertEquals(
                LambdaTerm("x", LambdaTerm("y", AppTerm(VarTerm("z")))),
                parser.Lambda("""\x.\y.z""").valueOrFail())
    }

    @Test fun term1() {
        assertEquals(VarTerm("A123"), parser.Term1("""A123""").valueOrFail())
        assertEquals(AppTerm(VarTerm("x")), parser.Term1("""(x)""").valueOrFail())
    }

    @Test fun term() {
        // lambda
        assertEquals(
                LambdaTerm("x", LambdaTerm("y", AppTerm(VarTerm("z")))),
                parser.Term("""\x.\y.z""").valueOrFail())
        // app
        assertEquals((AppTerm(VarTerm("A123"))), parser.Term("""A123""").valueOrFail())
    }

    @Test fun let() {
        assertEquals(
                LetTerm("x", AppTerm(VarTerm("y")), AppTerm(VarTerm("z"))) as Terminal,
                parser.Let("""let x = y in z""").valueOrFail())
    }

    @Test fun program() {
        assertNotNull(parser.All("\\x.y;"))
        assertTrue(parser.All("\\x.y").result is Result.ParseError, "do not match if semicolon missing")

        assertNotNull(parser.All("""
             let true = \x.\y.x in
             let false = \x.\y.y in
             let if = \b.\l.\r.(b l) r in
             if true then false else true;"""))
    }
}