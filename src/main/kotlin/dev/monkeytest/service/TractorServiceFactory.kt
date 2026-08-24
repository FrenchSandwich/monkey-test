package dev.monkeytest.service

/**
 * Creates a fresh, per-instance [TractorService]. Kept as its own abstraction so
 * [TractorFleet] doesn't need to know how a tractor's dependencies (occupancy grid,
 * event publisher, delays) are wired — that's the Spring prototype bean's job.
 */
fun interface TractorServiceFactory {
    fun create(id: TractorId, initialPosition: Position): TractorService
}
