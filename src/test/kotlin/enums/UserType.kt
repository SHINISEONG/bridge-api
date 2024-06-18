package enums

enum class UserType(val value: Int) {
    ADMIN(0),
    SELLER(1),
    BUYER(2),
    ;
    companion object {
        fun from(value: Int): UserType = entries.firstOrNull { it.value == value } ?: throw IllegalArgumentException()
    }
}