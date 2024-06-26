package errorHandler

import dto.res.ApiCommonResDto
import exceptions.PaymentException
import io.hss.bridgeApi.type.ErrorHandler
import vo.ErrorData

val serviceExceptionHandler = ErrorHandler { throwable ->
    when (throwable) {
        is PaymentException -> {
            val paymentType = throwable.paymentType
            val errMsg = throwable.message
            ApiCommonResDto(
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

        else -> null
    }
}