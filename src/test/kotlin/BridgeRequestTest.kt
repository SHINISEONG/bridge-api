import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import contorller.ProductController
import contorller.UserController
import dto.req.UserReqDto
import io.hss.bridgeApi.RouterRegistry
import io.hss.bridgeApi.enums.MethodType
import io.hss.bridgeApi.type.ApiCommonRequest
import io.hss.bridgeApi.util.serializeToJson
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import serializer.JacksonCustomSerializeModule

class BridgeRequestTest : StringSpec({
    val userController = UserController()
    val productController = ProductController()

    val objectMapper = ObjectMapper().findAndRegisterModules()
        .registerModules(JacksonCustomSerializeModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    val registry = RouterRegistry.builder()
        .setSerializer(objectMapper)
        .registerController("api/v1/users", userController)
        .registerController("api/v1/products", productController)
        .build()

    "GET: /api/v1/users/:id - path variable id=1" {
        val requestString = ApiCommonRequest(
            pathAndQuery = "api/v1/users/1",
            methodType = MethodType.GET,
            body = ""
        ).serializeToJson()
        registry.bridgeRequest(requestString) shouldBe userController.getUserById(1).serializeToJson(objectMapper)
    }

    "POST: /api/v1/users - create user" {
        val requestString = ApiCommonRequest(
            pathAndQuery = "api/v1/users",
            methodType = MethodType.POST,
            body = UserReqDto(name = "John", age = 20, type = 0)
        ).serializeToJson()
        registry.bridgeRequest(requestString) shouldBe userController.createUser(
            UserReqDto(
                name = "John",
                age = 20,
                type = 0
            )
        ).serializeToJson(objectMapper)
    }

    "PATCH: /api/v1/users/:id/user-type - update user type" {
        val requestString = ApiCommonRequest(
            pathAndQuery = "api/v1/users/1/user-type",
            methodType = MethodType.PATCH,
            body = UserReqDto(name = "John", age = 20, type = 1)
        ).serializeToJson()
        val userReq = UserReqDto(name = "John", age = 20, type = 1)
        registry.bridgeRequest(requestString) shouldBe userController.updateUserAge(1, userReq)
            .serializeToJson(objectMapper)
    }

    "DELETE: /api/v1/users/:id - delete user" {
        val requestString = ApiCommonRequest(
            pathAndQuery = "api/v1/users/1",
            methodType = MethodType.DELETE,
            body = ""
        ).serializeToJson()
        registry.bridgeRequest(requestString) shouldBe userController.deleteUser(1).serializeToJson(objectMapper)
    }

    "GET: /api/v1/users/all - get all users(ASC)" {
        val requestString = ApiCommonRequest(
            pathAndQuery = "api/v1/users/all?order=ASC",
            methodType = MethodType.GET,
            body = ""
        ).serializeToJson()
        registry.bridgeRequest(requestString) shouldBe userController.getAllUsers("ASC").serializeToJson(objectMapper)
    }

    "GET: /api/v1/users/all - get all users(DESC)" {
        val requestString = ApiCommonRequest(
            pathAndQuery = "api/v1/users/all?order=DESC",
            methodType = MethodType.GET,
            body = ""
        ).serializeToJson()
        registry.bridgeRequest(requestString) shouldBe userController.getAllUsers("DESC").serializeToJson(objectMapper)
    }
})
