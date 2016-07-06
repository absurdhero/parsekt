package net.raboof.parsekt


sealed class Consumable<TInput, TValue>(val result: Result<TInput, TValue>) {

    class Consumed<TInput, TValue>(result: Result<TInput, TValue>) : Consumable<TInput, TValue>(result)
    class Empty<TInput, TValue>(result: Result<TInput, TValue>) : Consumable<TInput, TValue>(result)

    fun <TValue2> withResult(newResult: Result<TInput, TValue2>): Consumable<TInput, TValue2> {
        return when (this) {
            is Consumed -> Consumed(newResult)
            is Empty -> Empty(newResult)
        }
    }

    companion object {
        public fun <TInput, TValue> factory(isConsumed: Boolean): (result: Result<TInput, TValue>) -> Consumable<TInput, TValue> {
            if (isConsumed) {
                return { result -> Consumable.Consumed(result) }
            } else {
                return { result -> Consumable.Empty(result) }
            }
        }
    }

    /** convenience method for tests and other error tolerant usage */
    fun valueOrFail() : TValue {
        return when (this.result) {
            is Result.ParseError -> throw RuntimeException("parse error: $this")
            is Result.Value -> this.result.value
        }
    }
}


