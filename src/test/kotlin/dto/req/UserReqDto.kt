package dto.req

import domain.User
import enums.UserType
import kotlin.random.Random


data class UserReqDto(
    val name: String,
    val age: Int,
    val type: Int,
) {
    fun toDomain(id:Long): User = User(
        id = id,
        name = name,
        age = age,
        type = UserType.from(type),
    )
}
