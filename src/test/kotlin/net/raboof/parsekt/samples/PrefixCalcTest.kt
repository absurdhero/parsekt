package net.raboof.parsekt.samples

import net.raboof.parsekt.Parser
import net.raboof.parsekt.Result
import org.junit.Test
import kotlin.collections.listOf
import kotlin.test.*
import kotlin.text.plus
import kotlin.text.substring

class PrefixCalcTest {

    public class PrefixCalcStringParser() : PrefixCalc<String>() {
        override val anyChar: Parser<String, Char>
            get() = Parser { input: String ->
                when (input.length) {
                    0 -> null
                    1 -> Result(input[0], "")
                    else -> Result(input[0], input.substring(1))
                }
            }
    }

    val parser = PrefixCalcStringParser();

    // These tests show the calculator in action

    @Test
    public fun evaluate() {
        check("+ 5 6", 11)
        check("- 20 8", 12)
        check("* 4 6", 24)
        check("/ 6 2", 3)
        check("- (* 10 10) (+ 1 1 1)", 97)
    }

    @Test
    public fun extraSpaces() {
        check(" - ( * 10 10 ) ( + 1 1 1 ) ", 97)
    }

    private fun check(input: String, value: Long) {
        assertEquals(value, parser.evaluate(input))
    }

    // These tests show how text is parsed into a tree structure

    @Test
    public fun number() {
        assertEquals(PrefixCalc.Number("123"), parser.number("123")?.value)
    }

    @Test
    public fun plusNegatives() {
        assertEquals(PrefixCalc.Operation('+', listOf(PrefixCalc.Number("-1"), PrefixCalc.Number("-123"))), parser.operation("+ -1 -123")?.value)
    }

    @Test
    public fun ops() {
        for(op in listOf('+', '*', '/', '-')) {
            assertEquals(PrefixCalc.Operation(op, listOf(PrefixCalc.Number("1"), PrefixCalc.Number("-123"))), parser.operation(op + " 1 -123")?.value)
        }
    }

    @Test
    public fun minusNegatives() {
        assertEquals(PrefixCalc.Operation('-', listOf(PrefixCalc.Number("-1"), PrefixCalc.Number("-123"))), parser.operation("- -1 -123")?.value)
    }

    @Test
    public fun nestedExpression() {
        assertEquals(PrefixCalc.Operation(
                '+',
                listOf(PrefixCalc.Number("-1"),
                PrefixCalc.Operation('+', listOf(PrefixCalc.Number("2"), PrefixCalc.Number("123"))))),
        parser.operation("+ -1 (+ 2 123)")?.value)
    }
}