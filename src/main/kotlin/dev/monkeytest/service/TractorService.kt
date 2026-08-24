package dev.monkeytest.service

import dev.monkeytest.config.logger
import org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicReference

@Component
@Scope(SCOPE_PROTOTYPE)
class TractorService(
    val id: TractorId,
    initialPosition: Position,
    private val occupancyGrid: OccupancyGrid,
    private val eventPublisher: ApplicationEventPublisher,
    private val turnDelayMs: Long = DEFAULT_TURN_DELAY_MS,
    private val advanceDelayMs: Long = DEFAULT_ADVANCE_DELAY_MS,
) {

    companion object {
        const val DEFAULT_TURN_DELAY_MS = 100L
        const val DEFAULT_ADVANCE_DELAY_MS = 500L
    }

    private val log = logger()
    private val position = AtomicReference(initialPosition)
    private val queue = LinkedBlockingQueue<Instruction>()
    private val worker = Thread(this::processQueue, "tractor-worker-${id.value}")

    init {
        occupancyGrid.register(id, initialPosition)
    }

    fun start() = worker.start()

    fun stop() = worker.interrupt()

    fun executeInstruction(instruction: Instruction) = queue.offer(instruction)

    fun currentPosition(): Position = position.get()

    private fun processQueue() {
        while (!Thread.currentThread().isInterrupted) {
            try {
                queue.take().run { execute(this) }
            } catch (_: InterruptedException) {
                log.warn("No more driving for now: tractor ${id.value} turned off.")
                Thread.currentThread().interrupt()
            }
        }
    }

    /**
     * @return the tractor's position once [instruction] has fully run (unchanged
     * from the starting position if every move it attempted was rejected).
     */
    internal fun execute(instruction: Instruction): Position = when (instruction) {
        Instruction.TurnRight -> {
            Thread.sleep(turnDelayMs)
            move { it.turnRight() }
        }

        Instruction.TurnLeft -> {
            Thread.sleep(turnDelayMs)
            move { it.turnLeft() }
        }

        is Instruction.Advance -> {
            var before = position.get()
            repeat(instruction.steps) {
                Thread.sleep(advanceDelayMs)
                val after = move { it.advance() }
                if (after == before) return after
                before = after
            }
            before
        }

        is Instruction.MoveToPosition -> move {
            Position(instruction.x, instruction.y, instruction.direction)
        }
    }

    /**
     * @return the tractor's position after attempting the move: the candidate
     * position if the [OccupancyGrid] accepted it, or the unchanged starting
     * position if it was rejected (outside the grid, or the target cell is
     * already occupied by another tractor).
     */
    private fun move(movement: (Position) -> Position): Position {
        val actualPosition = position.get()
        val candidatePosition = movement(actualPosition)

        if (!occupancyGrid.tryMove(id, actualPosition, candidatePosition)) {
            log.warn(
                "Tractor ${id.value} rejecting move to (${candidatePosition.x}, ${candidatePosition.y}): " +
                    "outside the grid or already occupied, staying at (${actualPosition.x}, ${actualPosition.y})"
            )
            return actualPosition
        }

        position.set(candidatePosition)
        log.info("Tractor ${id.value} moving $actualPosition to $candidatePosition")
        eventPublisher.publishEvent(PositionChangedEvent(id, candidatePosition))
        return candidatePosition
    }
}
