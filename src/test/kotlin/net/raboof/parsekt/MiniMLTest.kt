package net.raboof.parsekt

import net.raboof.parsekt.samples.MiniML
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
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

    @Test
    public fun ident() {
        val parser = MiniMLStringParser();
        assertEquals("A123", parser.Ident("""A123""")?.value)
    }

    @Test
    public fun lambda() {
        val parser = MiniMLStringParser();
        assertNotNull(parser.Lambda("""\x.y"""))
    }


    @Test
    public fun program() {
        val parser = MiniMLStringParser();
        assertNotNull(parser.All("""
             let true = \x.\y.x in
             let false = \x.\y.y in
             let if = \b.\l.\r.(b l) r in
             if true then false else true;"""))
    }
}