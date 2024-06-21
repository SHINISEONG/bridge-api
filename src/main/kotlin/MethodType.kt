import annotation.method.*

enum class MethodType {
    GET, POST, PATCH, DELETE, PUT
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
