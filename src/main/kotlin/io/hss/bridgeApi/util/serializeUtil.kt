package io.hss.bridgeApi.util

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

internal val defaultObjectMapper: ObjectMapper = jacksonObjectMapper()

internal inline fun <reified T> String.deserializeFromJson(
    objectMapper: ObjectMapper = defaultObjectMapper,
): T = objectMapper.readValue(this, object : TypeReference<T>() {})

internal fun Any.serializeToJson(
    objectMapper: ObjectMapper = defaultObjectMapper,
): String = objectMapper.writeValueAsString(this)