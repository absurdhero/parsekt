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
        assertEquals(Result.Value('t', "est"), parser.anyChar("test").result)
    }

    @Test
    fun whitespace() {
        assertEquals(Result.Value(emptyList<Char>(), "test"), parser.whitespace("test").result)
        assertEquals(Result.Value(listOf(' ', ' '), "test"), parser.whitespace("  test").result)
    }

    @Test
    fun chars() {
        assertTrue(parser.char('(')("x").result is Result.ParseError)
        assertEquals(Result.Value('(', "test)"), parser.char('(')("(test)").result)
        assertEquals(Result.Value(listOf('('), "test)"), parser.char('(').asList()("(test)").result)
    }

    @Test
    fun tokens() {
        assertEquals(Result.Value(listOf('a', 'b', 'c'), ""), parser.token("abc").result)

        // consumes whitespace both before and after
        assertEquals(Result.Value(listOf('a', 'b', 'c'), ""), parser.token(" abc ").result)
        assertEquals(Result.Value("test", ""), parser.token.string()(" test ").result)

        // does not match plain whitespace
        assertTrue(parser.token(" ").result is Result.ParseError)
    }

    @Test
    fun parenWrappedToken() {
        val parenWrappedToken = parser.token.between(
                parser.char('(') and parser.whitespace,
                parser.whitespace and parser.char(')'))

        assertEquals(Result.Value(listOf('x'), ""), parenWrappedToken("(x)").result)
        assertEquals(Result.Value("test", ""), parenWrappedToken.string()("(test)").result)
        assertEquals(Result.Value("test", " "), parenWrappedToken.string()("( test ) ").result)
    }

    @Test
    fun substring() {
        assertTrue(parser.substring(Regex("a"))("x").result is Result.ParseError)
        assertEquals(Result.Value("(", "test)"), parser.substring(Regex("\\(")).string()("(test)").result)
        assertEquals(Result.Value("(test", ")"), parser.substring(Regex("\\([^)]*")).string()("(test)").result)
        assertEquals(Result.Value("(test)", ""), parser.substring(Regex("\\([^)]+\\)")).string()("(test)").result)
        assertEquals(Result.Value("(test)", ""), parser.substring(Regex(".*")).string()("(test)").result)

        assertEquals(Result.Value("\"\\\"foo\"", " abc"), parser.substring(Regex(""""(\\.|[^\\"])*"""")).string()("\"\\\"foo\" abc").result)
    }


    @Test
    fun errorInformation() {
        val result = parser.concat(parser.char('b'), parser.char('o'), parser.char('p')).string()("bolt").result
        when (result) {
            is Result.ParseError -> {
                assertEquals("char(p)", result.productionLabel)
                assertEquals("lt", result.rest)
                assertEquals("Error{production=char(p), child=null, rest=lt}", result.toString())
                assertEquals("Error{production=char(p), child=null}", result.innerToString())
            }
            else -> fail()
        }
    }

}