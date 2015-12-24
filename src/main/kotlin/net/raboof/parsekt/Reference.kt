package net.raboof.parsekt

/** useful when declaring mutually recursive parsers */
class Reference<TInput, TValue> {
    private var parser: Parser<TInput, TValue> = Parser({throw NotImplementedError("parser reference not set") })

    public fun set(to : Parser<TInput, TValue>) {
        parser = to
    }

    public fun get() : Parser<TInput, TValue> {
        return Parser({input -> parser.invoke(input)})
    }
}