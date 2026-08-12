package dev.monkeytest.service

enum class CardinalPoint {
    N, E, S, O;

    fun turnRight() = when (this) {
        N -> E
        E -> S
        S -> O
        O -> N
    }

    fun turnLeft() = when (this) {
        N -> O
        E -> N
        S -> E
        O -> S
    }

}
