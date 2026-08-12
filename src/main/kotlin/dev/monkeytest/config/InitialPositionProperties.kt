package dev.monkeytest.config

import dev.monkeytest.service.CardinalPoint
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PositiveOrZero
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(prefix = "tractor.initial-position")
data class InitialPositionProperties(
    @PositiveOrZero @NotNull
    val x: Int = 0,
    @PositiveOrZero @NotNull
    val y: Int = 0,
    val direction: CardinalPoint = CardinalPoint.N,
)
