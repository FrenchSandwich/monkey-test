package dev.monkeytest.web

import dev.monkeytest.service.Position

data class TractorPositionPayload(val tractorId: String, val position: Position)
