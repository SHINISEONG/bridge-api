package io.github.shiniseong.bridgeApi.type.service.decorator

import io.github.shiniseong.bridgeApi.type.BridgeResponse
import io.github.shiniseong.bridgeApi.type.RequestContext
import io.github.shiniseong.bridgeApi.type.service.ServiceDecorator
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MeasureDecorator : ServiceDecorator() {
    private val logger: Logger = LoggerFactory.getLogger(MeasureDecorator::class.java)

    override fun serve(ctx: RequestContext): BridgeResponse {
        val start = System.currentTimeMillis()
        val response = unwrap().serve(ctx)
        val end = System.currentTimeMillis()
        logger.debug("Execution time: ${end - start}ms")
        return response
    }
}