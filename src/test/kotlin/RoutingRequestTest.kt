import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import contorller.ProductController
import contorller.UserController
import dto.req.UserReqDto
import io.github.shiniseong.bridgeApi.BridgeRouter
import io.github.shiniseong.bridgeApi.enums.MethodType
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import serializer.JacksonCustomSerializeModule

class RoutingRequestTest : StringSpec({
    val userController = UserController()
    val productController = ProductController()

    val objectMapper = ObjectMapper().findAndRegisterModules()
        .registerModules(JacksonCustomSerializeModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    val router = BridgeRouter.builder()
        .setSerializer(objectMapper)
        .registerController("api/v1/users", userController)
        .registerController("api/v1/products", productController)
        .build()

    "routingRequest" should {
        "GET: /api/v1/users/:id - path variable id=1" {
            router.routingRequest("api/v1/users/1", MethodType.GET) shouldBe """
                {"status":0,"message":"success","data":{"id":1,"name":"John","age":20,"type":1}}
            """.trimIndent()
        }

        "GET: /api/v1/users/all - get all users" {
            router.routingRequest("api/v1/users/all?order=ASC", MethodType.GET) shouldBe """
                {"status":0,"message":"success","data":[{"id":1,"name":"John","age":20,"type":0},{"id":2,"name":"Jane","age":22,"type":1}]}
            """.trimIndent()
        }

        "POST: /api/v1/users - create user" {
            val userDto = UserReqDto(
                name = "John",
                age = 20,
                type = 0
            )
            router.routingRequest(
                pathAndQueryString = "api/v1/users",
                method = MethodType.POST,
                body = userDto
            ) shouldBe """
                {"status":0,"message":"success","data":{"id":1,"name":"John","age":20,"type":0}}
            """.trimIndent()
        }

        "DELETE: /api/v1/users/:id - delete user" {
            router.routingRequest("api/v1/users/1", MethodType.DELETE) shouldBe """
                {"status":0,"message":"success","data":{}}
            """.trimIndent()
        }

        "PATCH: /api/v1/users/:id/user-type - update user age - ENUM 타입 테스트" {
            val body = mapOf("name" to "John", "age" to 30, "type" to "1")
            router.routingRequest(
                pathAndQueryString = "api/v1/users/1/user-type",
                method = MethodType.PATCH,
                body = body
            ) shouldBe """
                {"status":0,"message":"success","data":{"id":1,"name":"John","age":30,"type":1}}
            """.trimIndent()
        }

        "POST: /api/v1/users/test/header - 헤더 테스트" {
            router.routingRequest(
                pathAndQueryString = "api/v1/users/test/header",
                method = MethodType.POST,
                headers = mapOf("X-Token" to "testToken", "X-Heart-Beat" to "true")
            ) shouldBe """
                {"status":0,"message":"success","data":{"X-Token":"testToken","X-Heart-Beat":true}}
            """.trimIndent()
        }

        "GET: /api/v1/products/:id - path variable id=1" {
            router.routingRequest("api/v1/products/1", MethodType.GET) shouldBe """
                {"status":0,"message":"success","data":{"id":1,"name":"Apple","price":100,"stock":10}}
            """.trimIndent()
        }

        "POST: /api/v1/products - create product" {
            val productDto = mapOf("name" to "Apple", "price" to 100, "stock" to 10)
            router.routingRequest(
                pathAndQueryString = "api/v1/products",
                method = MethodType.POST,
                body = productDto
            ) shouldBe """
                {"status":0,"message":"success","data":{"id":1,"name":"Apple","price":100,"stock":10}}
            """.trimIndent()
        }

        "POST: /api/v1/products/all - create products - 자료구조List Serialization & Deserialization 테스트" {
            val body = listOf(
                mapOf("name" to "Apple", "price" to 100, "stock" to 10),
                mapOf("name" to "Banana", "price" to 200, "stock" to 20)
            )
            router.routingRequest(
                pathAndQueryString = "api/v1/products/all",
                method = MethodType.POST,
                body = body
            ) shouldBe """
                {"status":0,"message":"success","data":[{"id":1,"name":"Apple","price":100,"stock":10},{"id":2,"name":"Banana","price":200,"stock":20}]}
            """.trimIndent()
        }

        "POST: /api/v1/products/all - create products - 배열 안 단일 객체 테스트" {
            val body = listOf(
                mapOf("name" to "Banana", "price" to 200, "stock" to 20)
            )
            router.routingRequest(
                pathAndQueryString = "api/v1/products/all",
                method = MethodType.POST,
                body = body
            ) shouldBe """
                {"status":0,"message":"success","data":[{"id":1,"name":"Banana","price":200,"stock":20}]}
            """.trimIndent()
        }

        "DELETE: /api/v1/products/:id - delete product" {
            router.routingRequest("api/v1/products/1", MethodType.DELETE) shouldBe """
                {"status":0,"message":"delete success","data":{}}
            """.trimIndent()
        }

        "PATCH: /api/v1/products/:id/stock/:stock - update product stock" {
            router.routingRequest("api/v1/products/1/stock/20", MethodType.PATCH) shouldBe """
                {"status":0,"message":"success","data":{"id":1,"name":"Apple","price":100,"stock":20}}
            """.trimIndent()
        }

        "POST: TimeMeasure - 100000회 요청 시간 측정 /:id/name/:name/user-type/:type/age/:age"{
            val start = System.currentTimeMillis()
            repeat(100000) {
                router.routingRequest("api/v1/users/1/name/John/user-type/1/age/20", MethodType.POST)
            }
            val end = System.currentTimeMillis()
            println("TimeMeasure: ${end - start}ms")
        }
    }
})
