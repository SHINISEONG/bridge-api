package io.github.shiniseong.bridgeApi.type.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.shiniseong.bridgeApi.annotation.param.Header
import io.github.shiniseong.bridgeApi.annotation.param.JsonBody
import io.github.shiniseong.bridgeApi.annotation.param.PathVariable
import io.github.shiniseong.bridgeApi.annotation.param.Query
import io.github.shiniseong.bridgeApi.type.BridgeResponse
import io.github.shiniseong.bridgeApi.type.RequestContext
import io.github.shiniseong.bridgeApi.util.serializeToJson
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaType

internal class BaseService(private val objectMapper: ObjectMapper) : BridgeService {
    override fun serve(ctx: RequestContext): BridgeResponse = BridgeResponse(invokeFunction(ctx))

    private fun invokeFunction(
        ctx: RequestContext,
    ): Any? {
        // 함수의 파라미터를 처리합니다.
        val args = ctx.function.valueParameters.map { param ->
            when {
                param.findAnnotation<JsonBody>() != null -> {
                    val type = object : TypeReference<Any>() {
                        override fun getType() = param.type.javaType
                    }
                    objectMapper.readValue(ctx.body?.serializeToJson(), type)
                }

                param.findAnnotation<Header>() != null -> {
                    convertParamValue(param.type.javaType, ctx.headers[param.findAnnotation<Header>()!!.key])
                }

                param.findAnnotation<Query>() != null -> {
                    convertParamValue(param.type.javaType, ctx.queryParameters[param.findAnnotation<Query>()!!.key])
                }

                param.findAnnotation<PathVariable>() != null -> {
                    convertParamValue(
                        param.type.javaType,
                        ctx.pathVariables[param.findAnnotation<PathVariable>()!!.key]
                    )
                }

                else -> null
            }
        }.toTypedArray()
        // 함수를 호출하여 결과를 반환합니다.
        return ctx.function.call(ctx.controller, *args)
    }

    private fun convertParamValue(paramType: java.lang.reflect.Type, paramValue: String?): Any? = when (paramType) {
        String::class.java -> paramValue
        Int::class.java, java.lang.Integer::class.java -> paramValue?.toInt()
        Boolean::class.java, java.lang.Boolean::class.java -> paramValue?.toBoolean()
        Float::class.java, java.lang.Float::class.java -> paramValue?.toFloat()
        Double::class.java, java.lang.Double::class.java -> paramValue?.toDouble()
        Long::class.java, java.lang.Long::class.java -> paramValue?.toLong()
        Short::class.java, java.lang.Short::class.java -> paramValue?.toShort()
        Byte::class.java, java.lang.Byte::class.java -> paramValue?.toByte()
        Char::class.java, java.lang.Character::class.java -> paramValue?.firstOrNull()
        else -> paramValue
    }

}