import contorller.PaymentController
import errorHandler.serviceExceptionHandler
import errorHandler.universalExceptionHandler
import io.hss.bridgeApi.BridgeRouter
import io.hss.bridgeApi.enums.MethodType
import io.hss.bridgeApi.util.defaultObjectMapper
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ErrorHandleTest : StringSpec({
    val paymentController = PaymentController()
    val objectMapper = defaultObjectMapper
    val router =
        BridgeRouter.builder().apply {
            setSerializer(objectMapper)
            registerController("/api/v1/payments", paymentController)
            registerAllErrorHandlers(listOf(serviceExceptionHandler, universalExceptionHandler))
        }.build()


    "GET: /api/v1/payments/:id - path variable id=1" {
        val result = router.routingRequest("/api/v1/payments", MethodType.GET, "")
        result shouldBe """{"status":500,"message":"서버 에러가 발생했습니다. test throwable","data":null}"""

    }

    "POST: /api/v1/payments - create payment" {
        val result = router.routingRequest("/api/v1/payments", MethodType.POST, "")
        result shouldBe """{"status":500,"message":"[ 카드 ] 결제에 실패했습니다.\n사용이 불가능한 카드입니다.","data":{"message":"사용이 불가능한 카드입니다.","errorType":"PAYMENT","errorCode":"PAYMENT_ERROR","time":"20240626"}}"""
    }
})