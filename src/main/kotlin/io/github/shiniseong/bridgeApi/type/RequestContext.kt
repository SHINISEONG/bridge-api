package io.github.shiniseong.bridgeApi.type

import io.github.shiniseong.bridgeApi.enums.MethodType
import kotlin.reflect.KFunction

data class RequestContext(
    val segments: List<String>,
    val method: MethodType,
    val body: Any? = null,
    val headers: Map<String, String> = emptyMap(),
    val pathVariables: Map<String, String> = emptyMap(),
    val queryParameters: Map<String, String> = emptyMap(),
    val controller: Any,
    val function: KFunction<*>,
)
