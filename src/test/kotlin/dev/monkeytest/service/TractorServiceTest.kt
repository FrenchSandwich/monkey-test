package dev.monkeytest.service

import dev.monkeytest.config.Grid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationEventPublisher

private const val FAST_DELAY_MS = 1L

class TractorServiceTest {

    private val grid = Grid(width = 10, height = 10)
    private val occupancyGrid = OccupancyGrid(grid)
    private val publishedEvents = mutableListOf<Any>()
    private val eventPublisher = ApplicationEventPublisher { publishedEvents.add(it) }

    private fun tractor(initialPosition: Position) = TractorService(
        id = TractorId("test-tractor"),
        initialPosition = initialPosition,
        occupancyGrid = occupancyGrid,
        eventPublisher = eventPublisher,
        turnDelayMs = FAST_DELAY_MS,
        advanceDelayMs = FAST_DELAY_MS,
    )

    @Test
    fun `turning right changes the direction and publishes the new position`() {
        val service = tractor(Position(1, 1, CardinalPoint.N))

        val result = service.execute(Instruction.TurnRight)

        assertEquals(Position(1, 1, CardinalPoint.E), result)
        assertEquals(listOf(PositionChangedEvent(TractorId("test-tractor"), result)), publishedEvents)
    }

    @Test
    fun `advancing moves the position by the requested number of steps`() {
        val service = tractor(Position(1, 1, CardinalPoint.N))

        val result = service.execute(Instruction.Advance(3))

        assertEquals(Position(1, 4, CardinalPoint.N), result)
    }

    @Test
    fun `advancing stops as soon as it hits the edge of the grid and does not overshoot`() {
        val service = tractor(Position(1, 8, CardinalPoint.N))

        val result = service.execute(Instruction.Advance(5))

        assertEquals(Position(1, 9, CardinalPoint.N), result)
    }

    @Test
    fun `advancing into a cell occupied by another tractor is rejected`() {
        occupancyGrid.register(TractorId("blocker"), Position(1, 2, CardinalPoint.N))
        val service = tractor(Position(1, 1, CardinalPoint.N))

        val result = service.execute(Instruction.Advance(1))

        assertEquals(Position(1, 1, CardinalPoint.N), result)
        assertEquals(emptyList<Any>(), publishedEvents)
    }

    @Test
    fun `moving directly to a position outside the grid is rejected and keeps the previous position`() {
        val service = tractor(Position(1, 1, CardinalPoint.N))

        val result = service.execute(Instruction.MoveToPosition(20, 20, CardinalPoint.N))

        assertEquals(Position(1, 1, CardinalPoint.N), result)
    }
}
