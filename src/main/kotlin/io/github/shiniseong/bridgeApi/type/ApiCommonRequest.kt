package io.github.shiniseong.bridgeApi.type

import io.github.shiniseong.bridgeApi.enums.MethodType

data class ApiCommonRequest(
    val pathAndQuery: String,
    val method: MethodType,
    val body: Any,
)
