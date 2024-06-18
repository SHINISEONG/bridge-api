package domain

import enums.UserType

data class User(
    val id: Long,
    val name: String,
    val age: Int,
    val type: UserType,
)
