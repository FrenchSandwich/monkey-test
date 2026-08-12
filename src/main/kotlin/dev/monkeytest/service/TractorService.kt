package dev.monkeytest.service

import dev.monkeytest.config.Grid
import dev.monkeytest.config.logger
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.springframework.stereotype.Service
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicReference

@Service
class TractorService(initialPosition: Position, private val grid: Grid) {

    companion object {
        private const val TURN_DELAY_MS = 100L
        private const val ADVANCE_DELAY_MS = 500L
    }

    private val log = logger()
    private val position = AtomicReference(initialPosition)
    private val queue = LinkedBlockingQueue<Instruction>()
    private val worker = Thread(this::processQueue, "tractor-worker")

    @PostConstruct
    fun start() = worker.start()

    @PreDestroy
    fun stop() = worker.interrupt()

    fun executeInstruction(instruction: Instruction) = queue.offer(instruction)

    private fun processQueue() {
        while (!Thread.currentThread().isInterrupted) {
            try {
                queue.take().run { execute(this) }
            } catch (_: InterruptedException) {
                log.warn("No more driving for now: tractor turned off.")
                Thread.currentThread().interrupt()
            }
        }
    }

    private fun execute(instruction: Instruction) {
        when (instruction) {
            Instruction.TurnRight -> {
                Thread.sleep(TURN_DELAY_MS)
                move { it.turnRight() }
            }

            Instruction.TurnLeft -> {
                Thread.sleep(TURN_DELAY_MS)
                move { it.turnLeft() }
            }

            is Instruction.Advance -> {
                for (step in 1..instruction.steps) {
                    Thread.sleep(ADVANCE_DELAY_MS)
                    val hasMoved = move { it.advance() }
                    if (!hasMoved) break
                }
            }

            is Instruction.MoveToPosition -> {
                if (!grid.contains(instruction.x, instruction.y)) {
                    log.warn("Rejecting move (${instruction.x}, ${instruction.y}) outside the grid (${grid.width}, ${grid.height}), staying at: (${position.get().x}, ${position.get().y})")
                } else {
                    move {
                        Position(
                            instruction.x, instruction.y, instruction.direction
                        )
                    }
                }
            }
        }
    }

    /**
     * @return true if the movement succeed, false if outside the grid
     */
    private fun move(movement: (Position) -> Position): Boolean {
        val actualPosition = position.get()
        val candidatePosition = movement(actualPosition)

        if (!grid.contains(candidatePosition.x, candidatePosition.y)) {
            log.warn("Rejecting move {${candidatePosition.x}, ${candidatePosition.y})} outside the grid (${grid.width}, ${grid.height}), staying at: (${actualPosition.x}, ${actualPosition.y}")
            return false
        }

        position.set(candidatePosition)
        log.info("Moving $actualPosition to $candidatePosition")
        return true
    }
}
