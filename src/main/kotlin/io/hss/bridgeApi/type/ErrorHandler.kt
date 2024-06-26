package io.hss.bridgeApi.type

fun interface ErrorHandler {
    fun handle(cause: Throwable): Any?
}