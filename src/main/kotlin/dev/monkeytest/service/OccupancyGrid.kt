package dev.monkeytest.service

import dev.monkeytest.config.Grid
import org.springframework.stereotype.Component
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Single source of truth for "which tractor is on which cell".
 * Every move across the fleet goes through the same lock, so two tractors
 * racing for the same cell can never both win.
 */
@Component
class OccupancyGrid(private val grid: Grid) {

    private val lock = ReentrantLock()
    private val occupiedBy = mutableMapOf<Pair<Int, Int>, TractorId>()

    fun register(id: TractorId, position: Position) = lock.withLock {
        occupiedBy[position.x to position.y] = id
    }

    fun unregister(id: TractorId, position: Position) = lock.withLock {
        occupiedBy.remove(position.x to position.y)
    }

    /**
     * @return true if [id] now occupies [to], false if [to] is outside the grid
     * or already occupied by another tractor.
     */
    fun tryMove(id: TractorId, from: Position, to: Position): Boolean = lock.withLock {
        if (!grid.contains(to.x, to.y)) return@withLock false

        val targetCell = to.x to to.y
        val occupant = occupiedBy[targetCell]
        if (occupant != null && occupant != id) return@withLock false

        occupiedBy.remove(from.x to from.y)
        occupiedBy[targetCell] = id
        true
    }
}
