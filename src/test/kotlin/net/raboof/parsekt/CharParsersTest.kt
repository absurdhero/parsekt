package net.raboof.parsekt

import org.junit.Test
import kotlin.collections.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class CharParsersTest {

    private val parser = StringParser()

    @Test
    fun firstChar() {
        assertEquals(Result.Value('t', "est"), parser.anyChar("test"))
    }

    @Test
    fun whitespace() {
        assertEquals(Result.Value(emptyList<Char>(), "test"), parser.whitespace("test"))
        assertEquals(Result.Value(listOf(' ', ' '), "test"), parser.whitespace("  test"))
    }

    @Test
    fun chars() {
        assertTrue(parser.char('(')("x") is Result.ParseError)
        assertEquals(Result.Value('(', "test)"), parser.char('(')("(test)"))
        assertEquals(Result.Value(listOf('('), "test)"), parser.char('(').asList()("(test)"))
    }

    @Test
    fun tokens() {
        assertEquals(Result.Value(listOf('a', 'b', 'c'), ""), parser.token("abc"))

        // consumes whitespace both before and after
        assertEquals(Result.Value(listOf('a', 'b', 'c'), ""), parser.token(" abc "))
        assertEquals(Result.Value("test", ""), parser.token.string()(" test "))

        // does not match plain whitespace
        assertTrue(parser.token(" ") is Result.ParseError)
    }

    @Test
    fun parenWrappedToken() {
        val parenWrappedToken = parser.token.between(
                parser.char('(') and parser.whitespace,
                parser.whitespace and parser.char(')'))

        assertEquals(Result.Value(listOf('x'), ""), parenWrappedToken("(x)"))
        assertEquals(Result.Value("test", ""), parenWrappedToken.string()("(test)"))
        assertEquals(Result.Value("test", " "), parenWrappedToken.string()("( test ) "))
    }

    @Test
    fun substring() {
        assertTrue(parser.substring(Regex("a"))("x") is Result.ParseError)
        assertEquals(Result.Value("(", "test)"), parser.substring(Regex("\\(")).string()("(test)"))
        assertEquals(Result.Value("(test", ")"), parser.substring(Regex("\\([^)]*")).string()("(test)"))
        assertEquals(Result.Value("(test)", ""), parser.substring(Regex("\\([^)]+\\)")).string()("(test)"))
        assertEquals(Result.Value("(test)", ""), parser.substring(Regex(".*")).string()("(test)"))

        assertEquals(Result.Value("\"\\\"foo\"", " abc"), parser.substring(Regex(""""(\\.|[^\\"])*"""")).string()("\"\\\"foo\" abc"))
    }


    @Test
    fun errorInformation() {
        val result = parser.concat(parser.char('b'), parser.char('o'), parser.char('p')).string()("bolt")
        when (result) {
            is Result.ParseError -> {
                assertEquals("char(p)", result.productionLabel)
                assertEquals("t", result.rest)
                assertEquals("Error{production=char(p), child=null, rest=t}", result.toString())
                assertEquals("Error{production=char(p), child=null}", result.innerToString())
            }
            else -> fail()
        }
    }

}