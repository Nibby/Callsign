package codes.nibby.callsign.api

class TimelineLogger(internal val sink: TimelineLogSink) {

    fun recordEventStart(name: String): TimedEvent {
        val startTimeNs = System.nanoTime()
        val event = TimedEvent(name, startTimeNs);

        sink.writeEventStart(event);

        return event;
    }

    fun recordEventEnd(event: TimedEvent) {
        synchronized(event.lock) {
            if (event.saved) {
                throw IllegalStateException("recordEventEnd() cannot be called twice for event: " + event.name)
            }

            event.endTimeNs = System.nanoTime()
            event.saved = true
        }

        sink.writeEventEnd(event);
    }

    fun recordEvent(event: InstantEvent) {
        synchronized(event.lock) {
            if (event.saved) {
                throw IllegalStateException("recordEvent() cannot be called twice for event: " + event.name)
            }

            event.saved = true
        }

        sink.writeEvent(event);
    }
}