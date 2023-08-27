package codes.nibby.callsign.api

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
class InstantEvent(name: String, timeNs: Long) : Event(TYPE, name, timeNs, null) {

    companion object {
        const val TYPE = "i"
    }

    /**
     * Constructs an instant event whose event time is generated at the time of this object's
     * creation.
     *
     * @param name Name of this event
     */
    constructor(name: String) : this(name, System.nanoTime())

}