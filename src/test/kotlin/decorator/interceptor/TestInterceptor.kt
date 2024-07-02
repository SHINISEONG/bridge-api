package decorator.interceptor

import io.github.shiniseong.bridgeApi.type.BridgeResponse
import io.github.shiniseong.bridgeApi.type.RequestContext
import io.github.shiniseong.bridgeApi.type.service.ServiceDecorator

class TestInterceptor : ServiceDecorator() {
    override fun serve(ctx: RequestContext): BridgeResponse {
        println("Before TestInterceptor1")
        if ("test" in ctx.segments && "interceptor" in ctx.segments && ctx.method.isGet()) {
            return BridgeResponse("Intercepted by TestInterceptor1 and not reach controller")
        }
        val response = unwrap().serve(ctx)
        println("After TestInterceptor1")
        return response
    }
}

class TestInterceptor2 : ServiceDecorator() {
    override fun serve(ctx: RequestContext): BridgeResponse {
        println("Before TestInterceptor2")
        if ("test" in ctx.segments && "interceptor" in ctx.segments && ctx.method.isPost()) {
            return BridgeResponse("Intercepted by TestInterceptor2 and not reach controller")
        }
        val response = unwrap().serve(ctx)
        println("After TestInterceptor2")
        return response
    }
}

class TestInterceptor3 : ServiceDecorator() {
    override fun serve(ctx: RequestContext): BridgeResponse {
        println("Before TestInterceptor3")
        if ("test" in ctx.segments && "interceptor" in ctx.segments && ctx.method.isPatch()) {
            return BridgeResponse("Intercepted by TestInterceptor3 and not reach controller")
        }
        val response = unwrap().serve(ctx)
        println("After TestInterceptor3")
        return response
    }
}