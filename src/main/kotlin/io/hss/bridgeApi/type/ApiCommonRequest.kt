package io.hss.bridgeApi.type

import io.hss.bridgeApi.enums.MethodType

data class ApiCommonRequest(
    val pathAndQuery: String,
    val method: MethodType,
    val body: Any,
)
