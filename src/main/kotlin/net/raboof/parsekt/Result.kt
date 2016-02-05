package net.raboof.parsekt

/** A parser can return one of two Results. Success with a value or error information. */
sealed class Result<TInput, TValue> {

    class Value<TInput, TValue>(val value: TValue, val rest: TInput) : Result<TInput, TValue>() {
        override fun toString(): String {
            return "Value{value=$value, rest=$rest}"
        }

        override fun equals(other: Any?): Boolean{
            if (this === other) return true
            if (other?.javaClass != javaClass) return false

            other as Value<*, *>

            if (value != other.value) return false
            if (rest != other.rest) return false

            return true
        }

        override fun hashCode(): Int{
            var result = value?.hashCode() ?: 0
            result += 31 * result + (rest?.hashCode() ?: 0)
            return result
        }
    }

    class ParseError<TInput, TValue>(val productionLabel: String,
                                     val child: ParseError<TInput, *>?,
                                     val rest: TInput) : Result<TInput, TValue>() {

        /** make a parent of another error */
        constructor(production: String, error: ParseError<TInput, *>) : this(production, error, error.rest)

        /** no child */
        constructor(production: String, rest: TInput) : this(production, null, rest)

        /** copy constructor */
        constructor(error: ParseError<TInput, *>) : this(error.productionLabel, error.child, error.rest)

        override fun toString(): String {
            return "Error{production=$productionLabel, child=${child?.innerToString()}, rest=$rest"
        }

        fun innerToString(): String {
            return "Error{production=$productionLabel, child=${child?.innerToString()}}"
        }
    }

    /** convenience method for tests and other error tolerant usage */
    fun valueOrFail() : TValue {
        return when (this) {
            is ParseError -> throw RuntimeException("parse error: $this")
            is Value -> this.value
        }
    }
}


