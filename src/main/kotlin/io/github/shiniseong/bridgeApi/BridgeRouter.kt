package io.github.shiniseong.bridgeApi

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.shiniseong.bridgeApi.annotation.method.*
import io.github.shiniseong.bridgeApi.annotation.param.Header
import io.github.shiniseong.bridgeApi.annotation.param.JsonBody
import io.github.shiniseong.bridgeApi.annotation.param.PathVariable
import io.github.shiniseong.bridgeApi.annotation.param.Query
import io.github.shiniseong.bridgeApi.enums.MethodType
import io.github.shiniseong.bridgeApi.enums.toMethodType
import io.github.shiniseong.bridgeApi.type.ApiCommonRequest
import io.github.shiniseong.bridgeApi.type.ErrorHandler
import io.github.shiniseong.bridgeApi.type.RequestContext
import io.github.shiniseong.bridgeApi.type.service.BaseService
import io.github.shiniseong.bridgeApi.type.service.BridgeService
import io.github.shiniseong.bridgeApi.type.service.ServiceDecorator
import io.github.shiniseong.bridgeApi.type.service.decorator.defaultDecorators
import io.github.shiniseong.bridgeApi.util.deserializeFromJson
import io.github.shiniseong.bridgeApi.util.serializeToJson
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaType

/**
 * Data class representing route information.
 * @property function The KFunction associated with the route.
 * @property controller The controller instance handling the route.
 * @property method The HTTP method type for the route.
 */
private data class RouteInfo(
    val function: KFunction<*>,
    val controller: Any,
    val method: MethodType,
)

/**
 * Data class representing a node in the route tree.
 * @property children Child nodes of the current node.
 * @property routeInfoByMethod Mapping of HTTP method types to route information.
 * @property routeInfo The route information, if any, associated with this node.
 * @property isPathVariable Indicates if the node is a path variable.
 * @property pathVariableName The name of the path variable, if any.
 */
private data class RouteNode(
    val children: MutableMap<String, RouteNode> = mutableMapOf(),
    val routeInfoByMethod: MutableMap<MethodType, RouteInfo> = mutableMapOf(),
    var routeInfo: RouteInfo? = null,
    val isPathVariable: Boolean = false,
    val pathVariableName: String? = null,
)

/**
 * Main router class for handling API routes.
 * @property objectMapper The ObjectMapper used for JSON serialization/deserialization.
 * @property routeTree The root node of the route tree.
 * @property errorHandlers List of error handlers.
 */
class BridgeRouter private constructor(
    private val objectMapper: ObjectMapper = jacksonObjectMapper(),
    private val logger: Logger,
    private val routeTree: RouteNode = RouteNode(),
    private val decoratedService: BridgeService,
    private val errorHandlers: List<ErrorHandler> = emptyList(),
) {
    init {
        logger.debug("BridgeRouter initialized.")
    }

    companion object {
        /**
         * Creates a new Builder instance for configuring and building a BridgeRouter.
         * @return A new Builder instance.
         */
        fun builder(): Builder {
            return Builder()
        }
    }

    /**
     * Builder class for constructing a BridgeRouter instance.
     */
    class Builder {
        private var objectMapper: ObjectMapper = jacksonObjectMapper()
        private var logger: Logger = LoggerFactory.getLogger(BridgeRouter::class.java)
        private val controllers = mutableMapOf<String, Any>()
        private val errorHandlers: MutableList<ErrorHandler> = mutableListOf()
        private val serviceDecorators: MutableList<ServiceDecorator> = defaultDecorators
        private val routeTree = RouteNode()

        /**
         * Sets a custom serializer for JSON processing.
         * @param objectMapper The ObjectMapper to use.
         * @return The Builder instance for chaining.
         */
        fun setSerializer(objectMapper: ObjectMapper): Builder {
            this.objectMapper = objectMapper
            logger.debug("ObjectMapper set: {}", objectMapper)
            return this
        }

        fun setLogger(logger: Logger): Builder {
            this.logger = logger
            logger.debug("Logger set: {}", logger)
            return this
        }

        /**
         * Registers a controller with a specified path.
         * @param path The base path for the controller.
         * @param controller The controller instance.
         * @return The Builder instance for chaining.
         */
        fun registerController(path: String, controller: Any): Builder {
            controllers[path] = controller
            logger.debug("Controller registered: {}, {}", path, controller)
            return this
        }

        /**
         * Registers an error handler.
         * @param errorHandler The ErrorHandler instance.
         * @return The Builder instance for chaining.
         */
        fun registerErrorHandler(errorHandler: ErrorHandler): Builder {
            errorHandlers.add(errorHandler)
            logger.debug("ErrorHandler registered: {}", errorHandler)
            return this
        }

        /**
         * Registers multiple error handlers.
         * @param errorHandlers List of ErrorHandler instances.
         * @return The Builder instance for chaining.
         */
        fun registerAllErrorHandlers(errorHandlers: List<ErrorHandler>): Builder {
            this.errorHandlers.addAll(errorHandlers)
            logger.debug("ErrorHandlers registered: {}", errorHandlers)
            return this
        }

        fun registerDecorator(service: ServiceDecorator): Builder {
            serviceDecorators.add(service)
            logger.debug("Service registered: {}", service)
            return this
        }

        fun registerAllDecorators(services: List<ServiceDecorator>): Builder {
            this.serviceDecorators.addAll(services)
            logger.debug("Services registered: {}", services)
            return this
        }

        private fun buildDecoratedService(): BridgeService {
            return serviceDecorators
                .foldRight(BaseService(objectMapper) as BridgeService) { service, acc -> service.wrap(acc) }
        }

        /**
         * Builds and returns a configured BridgeRouter instance.
         * @return The configured BridgeRouter instance.
         */
        fun build(): BridgeRouter {
            buildRoutes()
            return BridgeRouter(
                objectMapper = objectMapper,
                logger = logger,
                routeTree = routeTree,
                decoratedService = buildDecoratedService(),
                errorHandlers = errorHandlers
            )
        }

        /**
         * Constructs the route tree from registered controllers and their annotated methods.
         */
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
            logger.debug("Routes built.")
        }

        /**
         * Adds a route to the route tree.
         * @param path The full path of the route.
         * @param routeInfo The route information.
         */
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

    /**
     * Handles an API request by bridging the request to the appropriate controller method.
     * @param apiCommonRequestString The API request as a JSON string.
     * @return The response as a JSON string.
     */
    fun bridgeRequest(apiCommonRequestString: String): String {
        val apiCommonRequest = apiCommonRequestString.deserializeFromJson<ApiCommonRequest>(objectMapper)
        val pathAndQuery = apiCommonRequest.pathAndQuery
        val method = apiCommonRequest.method
        val headers = apiCommonRequest.headers
        val body = apiCommonRequest.body

        return routingRequest(pathAndQuery, method, headers, body)
    }

    /**
     * Routes an incoming request to the appropriate controller method.
     * @param pathAndQueryString The path and query string of the request.
     * @param method The HTTP method of the request.
     * @param jsonStringBody The body of the request as a JSON string.
     * @return The response as a JSON string.
     */
    fun routingRequest(
        pathAndQueryString: String,
        method: MethodType,
        headers: Map<String, String> = emptyMap(),
        body: Any? = null,
    ): String = try {
        logger.debug("Routing request: {}, {}, {}, {}", pathAndQueryString, method, headers, body)

        val (path, queryString) = pathAndQueryString.split("?", limit = 2).let {
            it[0] to (it.getOrNull(1) ?: "")
        }

        val queryParams = queryString.split("&").mapNotNull {
            val (key, value) = it.split("=", limit = 2).let { it[0] to it.getOrNull(1) }
            if (key.isNotEmpty() && value != null) key to value else null
        }.toMap()


        val pathSegments = path.split("/").filter { it.isNotEmpty() }
        val (routeInfo, pathVariables) = findRoute(pathSegments, method)

        if (routeInfo != null) {
            val requestContext = RequestContext(
                segments = pathSegments,
                method = method,
                body = body,
                headers = headers,
                pathVariables = pathVariables,
                queryParameters = queryParams,
                controller = routeInfo.controller,
                function = routeInfo.function
            )
            val response = decoratedService.serve(requestContext).body
            logger.debug("Routing success.")
            response?.serializeToJson(objectMapper) ?: "{}"
        } else {
            logger.warn("Route not found.")
            "404"
        }
    } catch (throwable: Throwable) {
        // 예외 발생 시 에러 핸들러를 통해 처리합니다.
        val actualException = when (throwable) {
            is InvocationTargetException -> throwable.targetException
            else -> throwable
        }
        logger.error("Routing error: ${actualException.message}", actualException)
        // 에러 핸들러를 통해 처리된 결과를 반환합니다.
        val results = errorHandlers.mapNotNull { it.handle(actualException) }
        if (results.isEmpty()) "500" else results.first().serializeToJson(objectMapper)
    }

    /**
     * Finds the appropriate route in the route tree.
     * @param segments The path segments of the request.
     * @param method The HTTP method of the request.
     * @return A pair of RouteInfo and a map of path variables.
     */
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

    /**
     * Invokes the appropriate controller method with the resolved parameters.
     * @param controller The controller instance.
     * @param function The function to invoke.
     * @param queryParams The query parameters.
     * @param jsonStringBody The body of the request as a JSON string.
     * @param pathVariables The path variables.
     * @return The result of the function invocation.
     */
    private fun invokeFunction(
        controller: Any,
        function: KFunction<*>,
        queryParams: Map<String, String>,
        pathVariables: Map<String, String>,
        headers: Map<String, String>,
        jsonStringBody: String,
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
                    convertParamValue(param.type.javaType, headers[param.findAnnotation<Header>()!!.key])
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

    /**
     * Converts a string parameter value to the appropriate type.
     * @param paramType The target type.
     * @param paramValue The string value.
     * @return The converted value.
     */
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

/**
 * Ensures that a path string starts with a leading slash.
 * @return The path string with a leading slash.
 */
private fun Path.ensureLeadingSlash() = if (startsWith("/")) this else "/$this"
