package codes.nibby.callsign.api

/**
 * Publishes recorded event data from a [TimelineLogger] to some destination.
 */
interface TimelineLogSink {

    /**
     * Publishes a recorded event to the destination.
     *
     * @param event Event to publish
     */
    fun publishEvent(event: Event)

}