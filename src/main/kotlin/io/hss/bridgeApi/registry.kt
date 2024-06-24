package io.hss.bridgeApi

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.hss.bridgeApi.annotation.method.*
import io.hss.bridgeApi.annotation.param.Header
import io.hss.bridgeApi.annotation.param.JsonBody
import io.hss.bridgeApi.annotation.param.PathVariable
import io.hss.bridgeApi.annotation.param.Query
import io.hss.bridgeApi.enums.MethodType
import io.hss.bridgeApi.enums.toMethodType
import io.hss.bridgeApi.type.ApiCommonRequest
import io.hss.bridgeApi.util.deserializeFromJson
import io.hss.bridgeApi.util.serializeToJson
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaType

data class RouteInfo(
    val function: KFunction<*>,
    val controller: Any,
    val method: MethodType,
)

private data class RouteNode(
    val children: MutableMap<String, RouteNode> = mutableMapOf(),
    val routeInfoByMethod: MutableMap<MethodType, RouteInfo> = mutableMapOf(),
    var routeInfo: RouteInfo? = null,
    val isPathVariable: Boolean = false,
    val pathVariableName: String? = null,
)

class BridgeRouter private constructor(
    private val objectMapper: ObjectMapper = jacksonObjectMapper(),
    private val routeTree: RouteNode = RouteNode(),
) {
    // === companion object ===
    companion object {
        fun builder(): Builder {
            return Builder()
        }
    }

    // === Builder ===
    class Builder {
        private var objectMapper: ObjectMapper = jacksonObjectMapper()
        private val controllers = mutableMapOf<String, Any>()
        private val routeTree = RouteNode()

        fun setSerializer(objectMapper: ObjectMapper): Builder {
            this.objectMapper = objectMapper
            return this
        }

        fun registerController(path: String, controller: Any): Builder {
            controllers[path] = controller
            return this
        }

        fun build(): BridgeRouter {
            buildRoutes()
            return BridgeRouter(
                objectMapper = objectMapper,
                routeTree = routeTree
            )
        }


        private fun buildRoutes() {
            controllers.forEach { (path, controller) ->
                val controllerClass = controller::class
                for (function in controllerClass.memberFunctions) {
                    val matchedAnnotation = when {
                        function.findAnnotation<Get>() != null -> function.findAnnotation<Get>()
                        function.findAnnotation<Post>() != null -> function.findAnnotation<Post>()
                        function.findAnnotation<Patch>() != null -> function.findAnnotation<Patch>()
                        function.findAnnotation<Delete>() != null -> function.findAnnotation<Delete>()
                        function.findAnnotation<Put>() != null -> function.findAnnotation<Put>()
                        else -> null
                    }
                    val annotationPath = when (matchedAnnotation) {
                        is Get -> matchedAnnotation.path.ensureLeadingSlash()
                        is Post -> matchedAnnotation.path.ensureLeadingSlash()
                        is Patch -> matchedAnnotation.path.ensureLeadingSlash()
                        is Delete -> matchedAnnotation.path.ensureLeadingSlash()
                        is Put -> matchedAnnotation.path.ensureLeadingSlash()
                        else -> null
                    }

                    if (matchedAnnotation != null) {
                        val fullPath = path + annotationPath
                        val routeInfo = RouteInfo(
                            function = function,
                            controller = controller,
                            method = matchedAnnotation.toMethodType()
                        )
                        addRoute(fullPath, routeInfo)
                    }
                }
            }
        }


        private fun addRoute(path: String, routeInfo: RouteInfo) {
            val segments = path.split("/").filter { it.isNotEmpty() }
            tailrec fun add(currentNode: RouteNode, remainingSegments: List<String>) {
                if (remainingSegments.isEmpty()) {
                    currentNode.routeInfoByMethod[routeInfo.method] = routeInfo
                    return
                }

                val segment = remainingSegments.first()

                val nextNode =
                    if (segment.startsWith(":"))
                        currentNode.children.getOrPut("{pathVariable}") {
                            RouteNode(isPathVariable = true, pathVariableName = segment.substring(1))
                        }
                    else
                        currentNode.children.getOrPut(segment) { RouteNode() }

                add(nextNode, remainingSegments.drop(1))
            }
            add(routeTree, segments)
        }
    }

    // === public functions ===
    fun bridgeRequest(apiCommonRequestString: String): String {
        val apiCommonRequest = apiCommonRequestString.deserializeFromJson<ApiCommonRequest>(objectMapper)
        val pathAndQuery = apiCommonRequest.pathAndQuery
        val method = apiCommonRequest.method
        val bodyString = apiCommonRequest.body.serializeToJson(objectMapper)

        return routingRequest(pathAndQuery, method, bodyString)
    }

    fun routingRequest(pathAndQueryString: String, method: MethodType, jsonStringBody: String = ""): String {
        val (path, queryString) = pathAndQueryString.split("?", limit = 2).let {
            it[0] to (it.getOrNull(1) ?: "")
        }
        val queryParams = queryString.split("&").mapNotNull {
            val (key, value) = it.split("=", limit = 2).let { it[0] to it.getOrNull(1) }
            if (key.isNotEmpty() && value != null) key to value else null
        }.toMap()

        val pathSegments = path.split("/").filter { it.isNotEmpty() }
        val (routeInfo, pathVariables) = findRoute(pathSegments, method)

        return if (routeInfo != null) {
            val result =
                invokeFunction(routeInfo.controller, routeInfo.function, queryParams, jsonStringBody, pathVariables)
            result?.serializeToJson(objectMapper) ?: "{}"
        } else {
            "404"
        }
    }

    // === private functions ===
    private fun findRoute(segments: List<String>, method: MethodType): Pair<RouteInfo?, Map<String, String>> {
        tailrec fun find(
            currentNode: RouteNode,
            remainingSegments: List<String>,
            pathVariables: MutableMap<String, String>,
        ): Pair<RouteInfo?, Map<String, String>> {
            if (remainingSegments.isEmpty()) {
                return currentNode.routeInfoByMethod[method] to pathVariables
            }

            val segment = remainingSegments.first()
            val nextNode = currentNode.children[segment] ?: currentNode.children["{pathVariable}"]
            if (nextNode == null) return null to emptyMap()

            if (nextNode.isPathVariable) pathVariables[nextNode.pathVariableName!!] = segment

            return find(nextNode, remainingSegments.drop(1), pathVariables)
        }

        return find(routeTree, segments, mutableMapOf())
    }

    private fun invokeFunction(
        controller: Any,
        function: KFunction<*>,
        queryParams: Map<String, String>,
        jsonStringBody: String,
        pathVariables: Map<String, String>,
    ): Any? {
        val args = function.valueParameters.map { param ->
            when {
                param.findAnnotation<JsonBody>() != null -> {
                    val type = object : TypeReference<Any>() {
                        override fun getType() = param.type.javaType
                    }
                    objectMapper.readValue(jsonStringBody, type)
                }

                param.findAnnotation<Header>() != null -> {
                    // Header 처리 로직 추가 가능
                    null
                }

                param.findAnnotation<Query>() != null -> {
                    convertParamValue(param.type.javaType, queryParams[param.findAnnotation<Query>()!!.key])
                }

                param.findAnnotation<PathVariable>() != null -> {
                    convertParamValue(
                        param.type.javaType,
                        pathVariables[param.findAnnotation<PathVariable>()!!.key]
                    )
                }

                else -> null
            }
        }.toTypedArray()
        return function.call(controller, *args)
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
private typealias Path = String

private fun Path.ensureLeadingSlash() = if (startsWith("/")) this else "/$this"