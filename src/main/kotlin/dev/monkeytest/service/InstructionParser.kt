package dev.monkeytest.service

import org.springframework.stereotype.Component

@Component
class InstructionParser {

    fun parse(raw: String): Instruction = when {
        raw == "D" -> Instruction.TurnRight
        raw == "G" -> Instruction.TurnLeft
        ADVANCE_REGEX.matches(raw) -> {
            val (steps) = ADVANCE_REGEX.find(raw)!!.destructured
            Instruction.Advance(steps.toInt())
        }

        SET_POSITION_REGEX.matches(raw) -> {
            val (x, y, direction) = SET_POSITION_REGEX.find(raw)!!.destructured
            Instruction.MoveToPosition(x.toInt(), y.toInt(), CardinalPoint.valueOf(direction))
        }

        else -> throw IllegalArgumentException("Unrecognized instruction: $raw") //TODO custom exc
    }

    companion object {
        private val ADVANCE_REGEX = Regex("""^A\((\d+)\)$""")
        private val SET_POSITION_REGEX = Regex("""^S\((\d+),(\d+),([NSEO])\)$""")
    }
}