package errorHandler

import dto.res.ApiCommonResDto
import io.hss.bridgeApi.type.ErrorHandler

val universalExceptionHandler = ErrorHandler { throwable ->
    ApiCommonResDto(
        status = 500,
        message = "서버 에러가 발생했습니다. ${throwable.message}",
        data = null
    )
}