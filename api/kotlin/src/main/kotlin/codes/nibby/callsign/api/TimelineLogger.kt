package codes.nibby.callsign.api

/**
 * Main logging handler used to records time-based events to a [destination][TimelineLogSink].
 *
 * To record a one-off event, use [recordEvent]. For interval-based event logging, use [recordEventStart]
 * and [recordEventEnd].
 *
 * @param sink Destination to store all recorded events
 *
 * @see Event
 * @see InstantEvent
 * @see IntervalStartEvent
 * @see IntervalEndEvent
 */
class TimelineLogger(private val sink: TimelineLogSink) {

    /**
     * Records the start of an interval-based event. Calling this method records a start event and
     * returns an event reference. When the interval event completes some time later, call
     * [recordEventEnd] with the same reference to complete the event.
     *
     * An [IntervalStartEvent] can only be recorded once. Calling this method on the same event
     * twice will result in an [IllegalStateException].
     *
     * The logger publishes the attributes set on the event at the time of method invocation. After
     * the event start is recorded, the event attributes can still be modified until [recordEventEnd]
     * is called. The latest set of attributes will be transferred to the [IntervalEndEvent] generated
     * as part of that method call and published to the sink.
     *
     * @param startEvent Interval start event to log
     *
     * @see recordEventEnd
     */
    fun recordEventStart(startEvent: IntervalStartEvent) {
        synchronized(startEvent.lock) {
            if (startEvent.recorded) {
                throw IllegalStateException("$startEvent is already recorded")
            }

            startEvent.recorded = true
        }

        sink.publishEvent(startEvent)
    }

    /**
     * Records the completion of an interval event. [recordEventStart] must have been called on
     * [startEvent] before calling this method. An [IntervalEndEvent] will be published to the
     * sink, with attributes cloned from the start event.
     *
     * After this method, the event reference should be discarded. Any attempt to modify it will
     * throw [IllegalStateException].
     *
     * @param startEvent A previously recorded interval start event to complete
     * @param endTimeNs Approximate time (in nanoseconds) the interval event ended on
     *
     * @throws IllegalArgumentException If [startEvent] has not been recorded as started, or this
     *                                  method is called twice for the same event.
     */
    fun recordEventEnd(startEvent: IntervalStartEvent, endTimeNs: Long) {
        val endEvent: IntervalEndEvent

        synchronized(startEvent.lock) {
            if (!startEvent.recorded) {
                throw IllegalStateException("Must first call recordEventStart() for event: $startEvent")
            }
            if (startEvent.published) {
                throw IllegalStateException("recordEventEnd() cannot be called twice for event: $startEvent")
            }

            startEvent.published = true

            endEvent = IntervalEndEvent(null, startEvent.id, startEvent.name, endTimeNs)
            endEvent.loadAttributeData(startEvent.getAttributeData(), includeSpecialAttributes = false)
        }

        sink.publishEvent(endEvent)
    }

    /**
     * Records a one-off event that occurred on a single moment in time. This method must not be used more
     * than once for any [InstantEvent] object.
     *
     * @param event The event to record.
     */
    fun recordEvent(event: InstantEvent) {
        synchronized(event.lock) {
            if (event.published) {
                throw IllegalStateException("recordEvent() cannot be called twice for event: " + event.name)
            }

            event.published = true
        }

        sink.publishEvent(event)
    }
}