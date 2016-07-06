package net.raboof.parsekt

/** useful when declaring mutually recursive parsers */
class Reference<TInput, TValue> {
    private var parser: Parser<TInput, TValue> = Parser({ throw NullPointerException("parser reference not set") })

    fun set(to: Parser<TInput, TValue>) {
        parser = to
    }

    fun get(): Parser<TInput, TValue> {
        return Parser({ input -> parser.invoke(input) })
    }
}