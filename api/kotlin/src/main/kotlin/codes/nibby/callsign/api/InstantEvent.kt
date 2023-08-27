package codes.nibby.callsign.api

import java.util.*

/**
 * Represents a one-off [Event] that occurred at a single point in time.
 *
 * @param name Name of this event
 * @param timeNs Approximate time (in nanoseconds) this event occurred on
 *
 * @see Event
 * @see IntervalStartEvent
 * @see IntervalEndEvent
 * @see TimelineLogger
 */
class InstantEvent(
    existingId: UUID?,
    name: String,
    timeNs: Long
) : Event(existingId, null, TYPE, name, timeNs) {

    companion object {
        const val TYPE = "i"
    }

    constructor(name: String, timeNs: Long) : this(null, name, timeNs)

}