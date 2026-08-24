package dev.monkeytest.service

import dev.monkeytest.config.Grid
import dev.monkeytest.config.InitialPositionProperties
import dev.monkeytest.config.logger
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.springframework.stereotype.Service
import java.util.NoSuchElementException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@Service
class TractorFleet(
    private val grid: Grid,
    private val tractorServiceFactory: TractorServiceFactory,
    private val initialPositionProperties: InitialPositionProperties
) {

    private val log = logger()
    private val tractors = ConcurrentHashMap<TractorId, TractorService>()
    private val sequence = AtomicLong()

    @PostConstruct
    fun spawnDefaultTractor() {
        create(Position(initialPositionProperties.x, initialPositionProperties.y, initialPositionProperties.direction))
    }

    fun create(initialPosition: Position): TractorId {
        check(grid.contains(initialPosition.x, initialPosition.y)) {
            "Initial position (${initialPosition.x},${initialPosition.y}) is outside of the grid (${grid.width},${grid.height})"
        }

        val id = TractorId("tractor-${sequence.incrementAndGet()}")
        val tractor = tractorServiceFactory.create(id, initialPosition)
        tractors[id] = tractor
        tractor.start()
        log.info("Spawned $id at $initialPosition")
        return id
    }

    fun get(id: TractorId): TractorService =
        tractors[id] ?: throw NoSuchElementException("Unknown tractor: ${id.value}")

    fun all(): Collection<TractorService> = tractors.values

    @PreDestroy
    fun stopAll() = tractors.values.forEach { it.stop() }
}
