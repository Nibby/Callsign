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
     * After [recordEventEnd] is called, the event reference should be discarded. Any attempt to
     * modify it will fail.
     *
     * @param name Name of the event
     *
     * @return Reference of the new interval event
     */
    fun recordEventStart(name: String): IntervalStartEvent {
        val startTimeNs = System.nanoTime()
        val event = IntervalStartEvent(null, name, startTimeNs);

        sink.publishEvent(event)

        return event;
    }

    /**
     * Records the completion of a previously started interval-based event created from
     * [recordEventStart].
     *
     * After this method, the event reference should be discarded. Any attempt to modify it will
     * fail.
     *
     * @param event A previously recorded interval start event
     */
    fun recordEventEnd(event: IntervalStartEvent) {
        val endTimeNs = System.nanoTime()
        val endEvent: IntervalEndEvent

        synchronized(event.lock) {
            if (event.saved) {
                throw IllegalStateException("recordEventEnd() cannot be called twice for event: " + event.name)
            }

            event.saved = true

            endEvent = IntervalEndEvent(null, event.id, event.name, endTimeNs)
            endEvent.loadAttributeData(event.getAttributeData(), includeSpecialAttributes = false)
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
            if (event.saved) {
                throw IllegalStateException("recordEvent() cannot be called twice for event: " + event.name)
            }

            event.saved = true
        }

        sink.publishEvent(event)
    }
}