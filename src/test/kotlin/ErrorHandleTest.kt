import contorller.PaymentController
import errorHandler.ServiceExceptionHandler
import errorHandler.universalExceptionHandler
import io.github.shiniseong.bridgeApi.BridgeRouter
import io.github.shiniseong.bridgeApi.enums.MethodType
import io.github.shiniseong.bridgeApi.util.defaultObjectMapper
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ErrorHandleTest : StringSpec({
    val paymentController = PaymentController()
    val objectMapper = defaultObjectMapper
    val router =
        BridgeRouter.builder().apply {
            setSerializer(objectMapper)
            registerController("/api/v1/payments", paymentController)
            registerAllErrorHandlers(listOf(ServiceExceptionHandler(), universalExceptionHandler))
        }.build()


    "GET: /api/v1/payments/:id - path variable id=1" {
        val result =
            router.routingRequest(pathAndQueryString = "/api/v1/payments", method = MethodType.GET, body = "")
        result shouldBe """{"status":500,"message":"서버 에러가 발생했습니다. test throwable","data":null}"""

    }

    "POST: /api/v1/payments - create payment" {
        val result = router.routingRequest(
            pathAndQueryString = "/api/v1/payments",
            method = MethodType.POST,
            body = ""
        )
        result shouldBe """{"status":500,"message":"[ 카드 ] 결제에 실패했습니다.\n사용이 불가능한 카드입니다.","data":{"message":"사용이 불가능한 카드입니다.","errorType":"PAYMENT","errorCode":"PAYMENT_ERROR","time":"20240626"}}"""
    }
})