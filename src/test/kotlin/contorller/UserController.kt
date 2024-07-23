package contorller

import dto.req.UserReqDto
import dto.res.ApiCommonResDto
import dto.res.UserResDto
import dto.res.toResDto
import enums.UserType
import io.github.shiniseong.bridgeApi.annotation.method.Delete
import io.github.shiniseong.bridgeApi.annotation.method.Get
import io.github.shiniseong.bridgeApi.annotation.method.Patch
import io.github.shiniseong.bridgeApi.annotation.method.Post
import io.github.shiniseong.bridgeApi.annotation.param.Header
import io.github.shiniseong.bridgeApi.annotation.param.JsonBody
import io.github.shiniseong.bridgeApi.annotation.param.PathVariable
import io.github.shiniseong.bridgeApi.annotation.param.Query

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
    fun createUser(@JsonBody userReq: UserReqDto): ApiCommonResDto<UserResDto> {
        return ApiCommonResDto(
            status = 0,
            message = "success",
            data = userReq.toDomain(1).toResDto(),
        )
    }

    @Patch("/:id/user-type")
    fun updateUserAge(@PathVariable("id") id: Long, @JsonBody userReq: UserReqDto): ApiCommonResDto<UserResDto> {
        return ApiCommonResDto(
            status = 0,
            message = "success",
            data = userReq.toDomain(id).toResDto()
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
    fun getAllUsers(@Query("order") order: String): ApiCommonResDto<List<UserResDto>> {
        val data = listOf(
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
            )
        )
        return ApiCommonResDto(
            status = 0,
            message = "success",
            data = if (order == "ASC") data.sortedBy { it.id } else data.sortedByDescending { it.id }
        )
    }

    @Post("/test/header")
    fun headerTest(
        @Header("X-Token") xToken: String,
        @Header("X-Heart-Beat") xHeartBeat: Boolean,
    ) = ApiCommonResDto(
        status = 0,
        message = "success",
        data = mapOf(
            "X-Token" to xToken,
            "X-Heart-Beat" to xHeartBeat,
        )
    )

    @Post("/:id/name/:name/user-type/:type/age/:age")
    fun getUserByTypeAndAge(
        @PathVariable("id") id: Long,
        @PathVariable("name") name: String,
        @PathVariable("type") type: UserType,
        @PathVariable("age") age: Int
    ): ApiCommonResDto<UserResDto> = ApiCommonResDto(
        status = 0,
        message = "success",
        data = UserResDto(
            id = id,
            name = name,
            age = age,
            type = type,
        ),
    )


}