@file:Suppress("MemberVisibilityCanBePrivate")

package codes.nibby.callsign.api

import java.util.UUID

/**
 * Represents an event that has a start and stop time. This event measures the finishing point of
 * such an event. The counterpart to this event is [IntervalStartEvent].
 *
 * User code does not usually interact with this event due to the design of the [TimelineLogger] API
 * which only exposes the [IntervalStartEvent]. There are a few special behaviour for this event to
 * be mindful of:
 *
 * 1. Every interval end event has a [correlationId] referencing the [IntervalStartEvent.id] that
 *    it complements. This is not stored as a first-class property rather than a special attribute.
 *
 * 2. Calling [TimelineLogger.recordEventEnd] with the interval start event creates an event of
 *    this type, and transfers all attributes (including special attributes) set on the start event
 *    to this event. In other words, in the log sink, the start event will not contain any
 *    attributes -- it is this event that will store the recorded attributes on the start event.
 *
 * 3. As a consequence of #2, it is not possible for API consumer code to instantiate this event
 *    type and modify its attributes. The name of this event is copied from the start event.
 *
 * @param name Name of this event
 * @param timeNs Approximate time (in nanoseconds) this event happened on
 * @param correlationId The [IntervalStartEvent.id] this interval end event complements
 *
 * @see IntervalStartEvent
 * @see InstantEvent
 * @see Event
 * @see TimelineLogger
 */
class IntervalEndEvent internal constructor(
    existingId: UUID?,
    correlationId: UUID,
    name: String,
    timeNs: Long
) : Event(existingId, correlationId, TYPE, name, timeNs) {

    companion object {
        const val TYPE = "t-e"
    }

}