package io.github.shiniseong.bridgeApi.type.service

import io.github.shiniseong.bridgeApi.type.BridgeResponse
import io.github.shiniseong.bridgeApi.type.RequestContext

interface BridgeService {
    suspend fun serve(ctx: RequestContext): BridgeResponse
}

abstract class ServiceDecorator : BridgeService {
    private lateinit var nestedService: BridgeService

    fun wrap(service: BridgeService): BridgeService {
        this.nestedService = service
        return this
    }

    fun unwrap(): BridgeService = nestedService
}