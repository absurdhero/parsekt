package net.raboof.parsekt

data class Result<TInput, TValue>(val value: TValue, val rest: TInput)