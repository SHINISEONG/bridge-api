import annotation.*
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.valueParameters
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.util.regex.Pattern

import kotlin.reflect.jvm.javaType


class ControllerRegistry(
    private val objectMapper: ObjectMapper = jacksonObjectMapper()
) {
    private val controllers = mutableMapOf<String, Any>()

    fun registerController(path: String, controller: Any) {
        controllers[path] = controller
    }

    fun handleRequest(pathAndQueryString: String, method: MethodType, jsonStringBody: String): Any? {
        val (path, queryString) = pathAndQueryString.split("?", limit = 2).let {
            it[0] to (it.getOrNull(1) ?: "")
        }
        val queryParams = queryString.split("&").mapNotNull {
            val (key, value) = it.split("=", limit = 2).let { it[0] to it.getOrNull(1) }
            if (key.isNotEmpty() && value != null) key to value else null
        }.toMap()

        for ((basePath, controller) in controllers) {
            val controllerClass = controller::class
            for (function in controllerClass.memberFunctions) {

                val matchedAnnotation = when (method) {
                    MethodType.GET -> function.findAnnotation<Get>()
                    MethodType.POST -> function.findAnnotation<Post>()
                    MethodType.PATCH -> function.findAnnotation<Patch>()
                    MethodType.DELETE -> function.findAnnotation<Delete>()
                    MethodType.PUT -> function.findAnnotation<Put>()
                }
                val annotationPath = when (matchedAnnotation) {
                    is Get -> matchedAnnotation.path
                    is Post -> matchedAnnotation.path
                    is Patch -> matchedAnnotation.path
                    is Delete -> matchedAnnotation.path
                    is Put -> matchedAnnotation.path
                    else -> null
                }


                if (matchedAnnotation != null) {
                    val fullPath = basePath + annotationPath
                    val (pathPattern, groupNames) = createPathPattern(fullPath)
                    val matcher = pathPattern.matcher(path)
                    if (matcher.matches()) {
                        val pathVariables = groupNames.associateWith { matcher.group(it) }
                        val result = invokeFunction(controller, function, queryParams, jsonStringBody, pathVariables)
                        return objectMapper.writeValueAsString(result)
                    }
                }
            }
        }
        return null
    }

    private fun createPathPattern(path: String): Pair<Pattern, List<String>> {
        val groupNames = mutableListOf<String>()
        val regex = path.replace(Regex(":([a-zA-Z][a-zA-Z0-9]*)")) {
            val groupName = it.groupValues[1]
            groupNames.add(groupName)
            "(?<$groupName>[^/]+)"
        }
        return Pattern.compile(regex) to groupNames
    }


    private fun invokeFunction(
        controller: Any,
        function: KFunction<*>,
        queryParams: Map<String, String>,
        jsonStringBody: String,
        pathVariables: Map<String, String>
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
                param.findAnnotation<Param>() != null -> {
                    convertParamValue(param.type.javaType, queryParams[param.findAnnotation<Param>()!!.key])
                }
                param.findAnnotation<PathVariable>() != null -> {
                    convertParamValue(param.type.javaType, pathVariables[param.findAnnotation<PathVariable>()!!.key])
                }
                else -> null
            }
        }.toTypedArray()
        return function.call(controller, *args)
    }
}
private fun convertParamValue(paramType: java.lang.reflect.Type, paramValue: String?): Any? {
    return when (paramType) {
        String::class.java -> paramValue
        Int::class.java, java.lang.Integer::class.java -> paramValue?.toInt()
        Boolean::class.java, java.lang.Boolean::class.java -> paramValue?.toBoolean()
        else -> paramValue
    }
}