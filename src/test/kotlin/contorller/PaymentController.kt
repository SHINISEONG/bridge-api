package contorller

import exceptions.PaymentException
import io.github.shiniseong.bridgeApi.annotation.method.Get
import io.github.shiniseong.bridgeApi.annotation.method.Post

class PaymentController {
    @Get("")
    fun getAllPayments() {
        throw Throwable("test throwable")
    }

    @Post("")
    fun createPayment() {
        throw PaymentException("카드", "사용이 불가능한 카드입니다.")
    }
}