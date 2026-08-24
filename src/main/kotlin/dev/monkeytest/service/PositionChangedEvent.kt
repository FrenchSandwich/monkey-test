package dev.monkeytest.service

data class PositionChangedEvent(val tractorId: TractorId, val position: Position)
