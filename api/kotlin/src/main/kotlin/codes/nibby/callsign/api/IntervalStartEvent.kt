package codes.nibby.callsign.api

import java.util.*

/**
 * Represents an event that has a start and stop time. This event measures the starting point
 * of such an event. The counterpart to this event is [IntervalEndEvent].
 *
 * @param name Name of this event
 * @param timeNs Approximate time (in nanoseconds) this event occurred on
 *
 * @see IntervalEndEvent
 * @see InstantEvent
 * @see Event
 * @see TimelineLogger
 */
class IntervalStartEvent internal constructor(
    existingId: UUID?,
    name: String,
    timeNs: Long
) : Event(existingId, null, TYPE, name, timeNs) {

    internal var recorded: Boolean = false

    companion object {
        const val TYPE = "t-s"
    }

    constructor(name: String, timeNs: Long) : this(null, name, timeNs)

}