package dev.monkeytest.service

sealed interface Instruction {

    data object TurnRight : Instruction
    data object TurnLeft : Instruction
    data class Advance(val steps: Int) : Instruction
    data class MoveToPosition(val x: Int, val y: Int, val direction: CardinalPoint) : Instruction
}
