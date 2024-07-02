package errorHandler

import dto.res.ApiCommonResDto
import exceptions.PaymentException
import io.github.shiniseong.bridgeApi.type.ErrorHandler
import vo.ErrorData

class ServiceExceptionHandler : ErrorHandler {
    override fun handle(cause: Throwable): Any? {
        when (cause) {
            is PaymentException -> {
                val paymentType = cause.paymentType
                val errMsg = cause.message
                return ApiCommonResDto(
                    status = 500,
                    message = "[ $paymentType ] 결제에 실패했습니다.\n$errMsg",
                    data = ErrorData(
                        message = errMsg,
                        errorType = "PAYMENT",
                        errorCode = "PAYMENT_ERROR",
                        time = "20240626"
                    )
                )
            }

            else -> return null
        }
    }
}