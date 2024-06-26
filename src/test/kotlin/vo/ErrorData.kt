package vo

import java.util.*

data class ErrorData(
    val message: String = "Unknown error",
    val errorType: String = "UNKNOWN",
    val errorCode: String = UUID.randomUUID().toString(),
    val time: String = "20240626",
)
