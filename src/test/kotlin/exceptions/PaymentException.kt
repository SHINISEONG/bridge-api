package exceptions

class PaymentException(val paymentType: String, override val message: String) : Exception() {
}