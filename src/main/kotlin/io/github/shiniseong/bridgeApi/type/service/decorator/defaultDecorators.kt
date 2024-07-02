package io.github.shiniseong.bridgeApi.type.service.decorator

import io.github.shiniseong.bridgeApi.type.service.ServiceDecorator

val defaultDecorators: MutableList<ServiceDecorator> = mutableListOf(
    MeasureDecorator()
)