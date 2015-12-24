package net.raboof.parsekt

import org.junit.Test
import kotlin.collections.*
import kotlin.test.assertEquals

class CharParsersTest {

    private val parser = StringParser()

    @Test
    fun firstChar() {
        assertEquals(Result('t', "est"), parser.anyChar("test"))
    }

    @Test
    fun whitespace() {
        assertEquals(Result(emptyList(), "test"), parser.whitespace("test"))
        assertEquals(Result(listOf(' ', ' '), "test"), parser.whitespace("  test"))
    }

    @Test
    fun chars() {
        assertEquals(null, parser.char('(')("x"))
        assertEquals(Result('(', "test)"), parser.char('(')("(test)"))
        assertEquals(Result(listOf('('), "test)"), parser.char('(').asList()("(test)"))
    }

    @Test
    fun tokens() {
        assertEquals(Result(listOf('a', 'b', 'c'), ""), parser.token("abc"))

        // consumes whitespace both before and after
        assertEquals(Result(listOf('a', 'b', 'c'), ""), parser.token(" abc "))
        assertEquals(Result("test", ""), parser.token.string()(" test "))

        // does not match plain whitespace
        assertEquals(null, parser.token(" "))
    }

    @Test
    fun parenWrappedToken() {
        val parenWrappedToken = parser.token.between(
                parser.char('(') and parser.whitespace,
                parser.whitespace and parser.char(')'))

        assertEquals(Result(listOf('x'), ""), parenWrappedToken("(x)"))
        assertEquals(Result("test", ""), parenWrappedToken.string()("(test)"))
        assertEquals(Result("test", " "), parenWrappedToken.string()("( test ) "))
    }
}