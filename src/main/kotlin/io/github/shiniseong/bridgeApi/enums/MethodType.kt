package io.github.shiniseong.bridgeApi.enums

import io.github.shiniseong.bridgeApi.annotation.method.*

enum class MethodType {
    GET, POST, PATCH, DELETE, PUT
    ;

    fun isGet() = this == GET
    fun isPost() = this == POST
    fun isPatch() = this == PATCH
    fun isDelete() = this == DELETE
    fun isPut() = this == PUT
}

private typealias HttpMethodAnnotation = Annotation

fun HttpMethodAnnotation.toMethodType() = when (this) {
    is Get -> MethodType.GET
    is Post -> MethodType.POST
    is Patch -> MethodType.PATCH
    is Delete -> MethodType.DELETE
    is Put -> MethodType.PUT
    else -> throw IllegalArgumentException("Unsupported HttpMethodAnnotation: $this")
}
