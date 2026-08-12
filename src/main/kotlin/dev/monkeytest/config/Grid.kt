package dev.monkeytest.config

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PositiveOrZero
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(prefix = "tractor.grid")
data class Grid(
    @PositiveOrZero @NotNull
    val width: Int,
    @PositiveOrZero @NotNull
    val height: Int
) {
    fun contains(x: Int, y : Int) : Boolean = x in 0 until width && y in 0 until height
}
