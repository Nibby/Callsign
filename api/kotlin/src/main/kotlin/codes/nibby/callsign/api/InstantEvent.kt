package codes.nibby.callsign.api

import java.util.*

/**
 * Represents a one-off [Event] that occurred at a single point in time.
 *
 * @param name Name of this event
 * @param timeMs Approximate time (in millisecond) this event occurred on
 *
 * @see Event
 * @see IntervalStartEvent
 * @see IntervalEndEvent
 * @see TimelineLogger
 */
class InstantEvent(
    existingId: UUID?,
    name: String,
    timeMs: Long
) : Event(existingId, null, TYPE, name, timeMs) {

    companion object {
        const val TYPE = "i"
    }

    constructor(name: String, timeMs: Long) : this(null, name, timeMs)

}