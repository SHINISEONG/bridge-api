package dto.res

data class ApiCommonResDto<T>(
    val status: Int,
    val message: String,
    val data: T,
)
