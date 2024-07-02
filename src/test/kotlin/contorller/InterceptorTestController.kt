package contorller

import io.github.shiniseong.bridgeApi.annotation.method.Delete
import io.github.shiniseong.bridgeApi.annotation.method.Get
import io.github.shiniseong.bridgeApi.annotation.method.Patch
import io.github.shiniseong.bridgeApi.annotation.method.Post

// /api/v1/test/interceptor
class InterceptorTestController {
    @Get("")
    fun testInterceptor(): String {
        return "reach test interceptor get controller"
    }

    @Post("")
    fun testInterceptorPost(): String {
        return "reach test interceptor post controller"
    }

    @Patch("")
    fun testInterceptorPatch(): String {
        return "reach test interceptor patch controller"
    }

    @Delete("")
    fun testInterceptorDelete(): String {
        return "reach test interceptor delete controller"
    }
}