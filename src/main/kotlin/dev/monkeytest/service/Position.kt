package dev.monkeytest.service

data class Position(val x: Int, val y: Int, val direction: CardinalPoint) {

    fun turnRight() = copy(direction = direction.turnRight())
    fun turnLeft() = copy(direction = direction.turnLeft())
    fun advance() = when (direction) {
        CardinalPoint.N -> copy(y = y + 1)
        CardinalPoint.E -> copy(x = x + 1)
        CardinalPoint.S -> copy(y = y - 1)
        CardinalPoint.O -> copy(x = x - 1)
    }
}