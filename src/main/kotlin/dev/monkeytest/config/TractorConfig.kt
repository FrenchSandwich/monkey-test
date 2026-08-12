package dev.monkeytest.config

import dev.monkeytest.service.Position
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TractorConfig {

    @Bean
    @ConditionalOnMissingBean(Position::class)
    fun setInitialPosition(properties: InitialPositionProperties, grid: Grid): Position {
        check(grid.contains(properties.x, properties.y))
        { "Initial position (${properties.x},${properties.y}) is outside of the grid (${grid.width},${grid.width})" }
        return Position(properties.x, properties.y, properties.direction)
    }
}