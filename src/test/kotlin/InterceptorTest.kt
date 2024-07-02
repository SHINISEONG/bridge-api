import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import contorller.InterceptorTestController
import contorller.ProductController
import contorller.UserController
import decorator.interceptor.TestInterceptor
import decorator.interceptor.TestInterceptor2
import decorator.interceptor.TestInterceptor3
import io.github.shiniseong.bridgeApi.BridgeRouter
import io.github.shiniseong.bridgeApi.enums.MethodType
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import serializer.JacksonCustomSerializeModule

class InterceptorTest : StringSpec({
    val userController = UserController()
    val productController = ProductController()
    val interceptorTestController = InterceptorTestController()

    val objectMapper = ObjectMapper().findAndRegisterModules()
        .registerModules(JacksonCustomSerializeModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    val router = BridgeRouter.builder()
        .apply {
            setSerializer(objectMapper)
            registerController("api/v1/users", userController)
            registerController("api/v1/products", productController)
            registerController("api/v1/test/interceptor", interceptorTestController)
            registerDecorator(TestInterceptor())
            registerDecorator(TestInterceptor2())
            registerDecorator(TestInterceptor3())
        }.build()

    "GET: /api/v1/users/:id - path variable id=1" {
        router.routingRequest("api/v1/users/1", MethodType.GET) shouldBe """
            {"status":0,"message":"success","data":{"id":1,"name":"John","age":20,"type":1}}
        """.trimIndent()
    }

    "GET: /api/v1/test/interceptor" {
        router.routingRequest("api/v1/test/interceptor", MethodType.GET) shouldBe """
            "Intercepted by TestInterceptor1 and not reach controller"
        """.trimIndent()
    }

    "POST: /api/v1/test/interceptor" {
        router.routingRequest("api/v1/test/interceptor", MethodType.POST) shouldBe """
            "Intercepted by TestInterceptor2 and not reach controller"
        """.trimIndent()
    }

    "PATCH: /api/v1/test/interceptor" {
        router.routingRequest("api/v1/test/interceptor", MethodType.PATCH) shouldBe """
            "Intercepted by TestInterceptor3 and not reach controller"
        """.trimIndent()
    }

    "DELETE: /api/v1/test/interceptor" {
        router.routingRequest("api/v1/test/interceptor", MethodType.DELETE) shouldBe """
            "reach test interceptor delete controller"
        """.trimIndent()
    }
})
