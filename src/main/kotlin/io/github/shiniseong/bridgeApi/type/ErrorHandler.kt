package io.github.shiniseong.bridgeApi.type

fun interface ErrorHandler {
    fun handle(cause: Throwable): Any?
}