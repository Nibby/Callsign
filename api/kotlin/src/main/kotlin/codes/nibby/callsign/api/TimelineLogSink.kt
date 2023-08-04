package codes.nibby.callsign.api

interface TimelineLogSink {

    fun writeEventStart(event: TimedEvent)

    fun writeEventEnd(event: TimedEvent)

    fun writeEvent(event: InstantEvent)

}