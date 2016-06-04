package net.raboof.parsekt

import org.junit.Test

import kotlin.test.assertEquals
import kotlin.test.fail

class ReferenceTest {
    @Test
    fun setAndGet() {
        val exprRef: Reference<String, Char> = Reference()
        val parser = StringParser().char('x')
        exprRef.set(parser)

        // use the error production label to figure out that the wrapped
        // parser is still calling the original char parser underneath

        val result = exprRef.get()("y")
        when (result) {
            is Result.ParseError -> assertEquals("char(x)", result.productionLabel)
            else -> fail()
        }
    }

    @Test
    fun throwWhenInvokedIfNotSet() {
        val exprRef: Reference<String, Char> = Reference()

        try {
            exprRef.get()("y")
        } catch(ignored: NullPointerException) {
            return
        }

        fail()
    }

}