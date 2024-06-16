import annotation.*
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper

data class SampleRequest(val name: String, val age: Int)
data class SampleApiResponse<T>(val status: String, val message: String, val data: T)
data class Human(val name: String, val age: Int)
class SampleController {
    @Get("/sample/get")
    fun getSample(@Param("id") id: Int): List<Int> {
        return listOf(id)
    }

    @Post("/sample/post")
    fun postSample(@JsonBody request: SampleRequest): SampleApiResponse<List<Human>> {
        return SampleApiResponse("success", "Received POST request with body: $request", data = listOf(
            Human("Alice", 25),
            Human("Bob", 30)
        ))
    }

    @Patch("/samples/:id/sample-field/:value")
    fun patchSample(@PathVariable("id") id: Int, @PathVariable("value") value: String): SampleApiResponse<String> {
        return SampleApiResponse("success", "Received PATCH request with id: $id and value: $value", data = "OK")
    }
}
class SampleController2 {
    @Get("/sample/get")
    fun getSample(@Param("id") id: Int): List<Int> {
        return listOf(id)
    }

    @Post("/sample/post")
    fun postSample(@JsonBody request: SampleRequest): SampleApiResponse<List<Human>> {
        return SampleApiResponse("success", "Received POST request with body: $request", data = listOf(
            Human("Alice", 25),
            Human("Bob", 30)
        ))
    }

    @Patch("/samples/:id/sample-field/:value")
    fun patchSample(@PathVariable("id") id: Int, @PathVariable("value") value: String): SampleApiResponse<String> {
        return SampleApiResponse("success", "Received PATCH request with id: $id and value: $value", data = "OK")
    }
}
fun main() {
    val sampleController = SampleController()
    val objectMapper = ObjectMapper().findAndRegisterModules()
        .registerModules(JacksonCustomSerializeModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    val registry = ControllerRegistry(objectMapper).apply {
        registerController("api/path", sampleController)
    }

    val getResponse = registry.handleRequest("api/path/sample/get?id=123", MethodType.GET, "")
    println(getResponse) // Expected output: Received GET request with id: 123

    val jsonBody = """{"name": "John", "age": 30}"""
    val postResponse = registry.handleRequest("api/path/sample/post", MethodType.POST, jsonBody)
    println(postResponse) // Expected output: Received POST request with body: SampleRequest(name=John, age=30)

    val patchResponse = registry.handleRequest("api/path/samples/123/sample-field/abc", MethodType.PATCH, "")
    println(patchResponse) // Expected output: Received PATCH request with id: 123 and value: abc
}