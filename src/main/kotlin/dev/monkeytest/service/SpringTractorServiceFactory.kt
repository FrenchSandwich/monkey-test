package dev.monkeytest.service

import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

/**
 * [ObjectProvider.getObject] with explicit args overrides ALL of the prototype bean's
 * constructor arguments, not just the ones supplied — it doesn't mix explicit args with
 * autowiring for the rest. So this factory resolves the autowired dependencies itself and
 * passes the full argument list through.
 */
@Component
class SpringTractorServiceFactory(
    private val tractorServices: ObjectProvider<TractorService>,
    private val occupancyGrid: OccupancyGrid,
    private val eventPublisher: ApplicationEventPublisher,
    @Value("\${tractor.turn-delay-ms:100}") private val turnDelayMs: Long,
    @Value("\${tractor.advance-delay-ms:500}") private val advanceDelayMs: Long,
) : TractorServiceFactory {

    override fun create(id: TractorId, initialPosition: Position): TractorService =
        tractorServices.getObject(id, initialPosition, occupancyGrid, eventPublisher, turnDelayMs, advanceDelayMs)
}
