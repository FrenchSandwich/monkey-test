package dev.monkeytest.web

import dev.monkeytest.config.logger
import dev.monkeytest.service.Position
import dev.monkeytest.service.PositionChangedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.CopyOnWriteArrayList

@Component
class PositionBroadcaster {

    companion object {
        private const val EMITTER_TIMEOUT_MS = 0L // never times out
    }

    private val log = logger()
    private val emitters = CopyOnWriteArrayList<SseEmitter>()

    fun subscribe(currentPosition: Position): SseEmitter {
        val emitter = SseEmitter(EMITTER_TIMEOUT_MS)
        emitter.onCompletion { emitters.remove(emitter) }
        emitter.onTimeout { emitters.remove(emitter) }
        emitter.onError { emitters.remove(emitter) }

        emitters.add(emitter)
        sendTo(emitter, currentPosition)

        return emitter
    }

    @EventListener
    fun onPositionChanged(event: PositionChangedEvent) = emitters.forEach { sendTo(it, event.position) }

    private fun sendTo(emitter: SseEmitter, position: Position) {
        try {
            emitter.send(SseEmitter.event().name("position").data(position))
        } catch (e: Exception) {
            log.warn("Dropping stale tractor position subscriber", e)
            emitters.remove(emitter)
        }
    }
}
