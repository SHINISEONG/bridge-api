package dto.res

import domain.User
import enums.UserType

data class UserResDto(
    val id: Long,
    val name: String,
    val age: Int,
    val type: UserType,
)

fun User.toResDto(): UserResDto = UserResDto(
    id = id,
    name = name,
    age = age,
    type = type,
)
