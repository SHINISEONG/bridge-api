package contorller

import annotation.*
import dto.req.UserReqDto
import dto.res.ApiCommonResDto
import dto.res.UserResDto
import dto.res.toResDto
import enums.UserType

// /api/v1/users
class UserController {
    @Get("/:id")
    fun getUserById(@PathVariable("id") id: Long): ApiCommonResDto<UserResDto> {
        return ApiCommonResDto(
            status = 0,
            message = "success",
            data = UserResDto(
                id = id,
                name = "John",
                age = 20,
                type = UserType.SELLER,
            ),
        )
    }

    @Post("")
    fun createUser(): ApiCommonResDto<UserResDto> {
        return ApiCommonResDto(
            status = 0,
            message = "success",
            data = UserResDto(
                id = 1,
                name = "John",
                age = 20,
                type = UserType.ADMIN,
            ),
        )
    }

    @Patch("/:id/user-type")
    fun updateUserAge(@PathVariable("id") id: Long, @JsonBody userReq: UserReqDto): ApiCommonResDto<UserResDto> {
        return ApiCommonResDto(
            status = 0,
            message = "success",
            data =userReq.toDomain(id).toResDto()
        )
    }

    @Delete("/:id")
    fun deleteUser(@PathVariable("id") id: Long): ApiCommonResDto<Unit> {
        return ApiCommonResDto(
            status = 0,
            message = "success",
            data = Unit,
        )
    }

    @Get("all")
    fun getAllUsers(): ApiCommonResDto<List<UserResDto>> {
        return ApiCommonResDto(
            status = 0,
            message = "success",
            data = listOf(
                UserResDto(
                    id = 1,
                    name = "John",
                    age = 20,
                    type = UserType.ADMIN,
                ),
                UserResDto(
                    id = 2,
                    name = "Jane",
                    age = 22,
                    type = UserType.SELLER,
                ),
            ),
        )
    }

}