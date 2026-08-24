package dev.monkeytest.web

import dev.monkeytest.service.CardinalPoint
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PositiveOrZero

data class CreateTractorRequest(
    @field:PositiveOrZero
    val x: Int,
    @field:PositiveOrZero
    val y: Int,
    @field:NotNull
    val direction: CardinalPoint,
)
