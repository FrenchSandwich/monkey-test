package dev.monkeytest.service

import dev.monkeytest.config.Grid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class OccupancyGridTest {

    private val grid = Grid(width = 10, height = 10)
    private val occupancyGrid = OccupancyGrid(grid)

    @RepeatedTest(20)
    fun `only one tractor can win a cell when several move into it concurrently`() {
        val target = Position(5, 5, CardinalPoint.N)
        val contenders = (0 until 10).map { TractorId("tractor-$it") }

        // each contender starts on its own distinct cell so only the race on `target` is observed
        contenders.forEachIndexed { index, id -> occupancyGrid.register(id, Position(0, index, CardinalPoint.N)) }

        val successes = AtomicInteger(0)
        val readyLatch = CountDownLatch(contenders.size)
        val startLatch = CountDownLatch(1)
        val executor: ExecutorService = Executors.newFixedThreadPool(contenders.size)

        try {
            val futures = contenders.mapIndexed { index, id ->
                executor.submit {
                    readyLatch.countDown()
                    startLatch.await()
                    if (occupancyGrid.tryMove(id, Position(0, index, CardinalPoint.N), target)) {
                        successes.incrementAndGet()
                    }
                }
            }

            readyLatch.await()
            startLatch.countDown()
            futures.forEach { it.get() }
        } finally {
            executor.shutdown()
        }

        assertEquals(1, successes.get(), "exactly one tractor must win the race for the contested cell")
    }

    @Test
    fun `a tractor can move into a cell it already occupies`() {
        val id = TractorId("tractor-0")
        val from = Position(1, 1, CardinalPoint.N)
        occupancyGrid.register(id, from)

        assertEquals(true, occupancyGrid.tryMove(id, from, from))
    }

    @Test
    fun `a move outside the grid is rejected`() {
        val id = TractorId("tractor-0")
        val from = Position(0, 0, CardinalPoint.N)
        occupancyGrid.register(id, from)

        assertEquals(false, occupancyGrid.tryMove(id, from, Position(-1, 0, CardinalPoint.N)))
    }
}
