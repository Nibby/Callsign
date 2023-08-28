package codes.nibby.callsign.api

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TimelineLoggerTest {

    @Test
    fun testRecordEventStart_setsEventRecordedFlag() {
        val timeline = TimelineLogger(TestSink())
        val event = IntervalStartEvent("timed event 1", System.nanoTime())

        timeline.recordEventStart(event)

        assertTrue(event.recorded)
    }

    @Test
    fun testRecordEventStart_invokeTwiceOnSameEvent_throwsIllegalStateException() {
        val timeline = TimelineLogger(TestSink())
        val event = IntervalStartEvent("timed event 1", System.nanoTime())

        timeline.recordEventStart(event)

        org.junit.jupiter.api.assertThrows<IllegalStateException> {
            timeline.recordEventStart(event)
        }
    }

    @Test
    fun testRecordEventStart_invokesSinkWriterMethod() {
        val sink = TestSink()
        val timeline = TimelineLogger(sink)
        val event = IntervalStartEvent("timed event 1", System.nanoTime())

        timeline.recordEventStart(event)

        assertTrue(sink.writeEventStartCalled.contains(event))
    }

    @Test
    fun testRecordEventEnd_endTimeSet() {
        val timeline = TimelineLogger(TestSink())
        val timedEvent = IntervalStartEvent("timed event 1", System.nanoTime())
        timeline.recordEventStart(timedEvent)

        timeline.recordEventEnd(timedEvent, System.nanoTime())

        assertNotNull(timedEvent)
    }

    @Test
    fun testRecordEventEnd_invokedTwiceOnSameEvent_failsWithIllegalStateException() {
        val timeline = TimelineLogger(TestSink())
        val timedEvent = IntervalStartEvent("timed event 1", System.nanoTime())
        timeline.recordEventStart(timedEvent)

        assertDoesNotThrow {
            timeline.recordEventEnd(timedEvent, System.nanoTime())
        }

        assertThrows(IllegalStateException::class.java) {
            timeline.recordEventEnd(timedEvent, System.nanoTime())
        }
    }

    @Test
    fun testRecordEventEnd_marksEventAsSaved() {
        val timeline = TimelineLogger(TestSink())
        val timedEvent = IntervalStartEvent("timed event 1", System.nanoTime())
        timeline.recordEventStart(timedEvent)

        timeline.recordEventEnd(timedEvent, System.nanoTime())

        assertTrue(timedEvent.published)
    }

    @Test
    fun testRecordEventEnd_eventStartNotRecorded_failsWithIllegalStateException() {
        val timeline = TimelineLogger(TestSink())
        val timedEvent = IntervalStartEvent("timed event 1", System.nanoTime())

        org.junit.jupiter.api.assertThrows<IllegalStateException> {
            timeline.recordEventEnd(timedEvent, System.nanoTime())
        }
    }

    @Test
    fun testRecordEventEnd_copiesAttributesFromIntervalStartEvent() {
        val sink = TestSink()
        val timeline = TimelineLogger(sink)
        val timedEvent = IntervalStartEvent("timed event 1", System.nanoTime())
        timedEvent.putAttribute("a1", "v1")
        timedEvent.putAttribute("a2", "v2")

        timeline.recordEventStart(timedEvent)

        timedEvent.putAttribute("a3", "v3")
        timedEvent.putAttribute("a1", "modified")

        timeline.recordEventEnd(timedEvent, System.nanoTime())

        assertEquals(1, sink.writeEventEndCalled.size)

        val endEvent = sink.writeEventEndCalled[0]

        assertEquals(timedEvent.getAttributeData(), endEvent.getAttributeData())
    }

    @Test
    fun testRecordEvent_marksEventAsSaved() {
        val event = InstantEvent("myEvent", System.nanoTime())
        val timeline = TimelineLogger(TestSink())

        timeline.recordEvent(event)

        assertTrue(event.published)
    }

    @Test
    fun testRecordEvent_invokesSinkWriterMethod() {
        val event = InstantEvent("myEvent", System.nanoTime())
        val sink = TestSink()
        val timeline = TimelineLogger(sink)

        timeline.recordEvent(event)

        assertTrue(sink.writeEventCalled.contains(event))
    }

    @Test
    fun testRecordEvent_methodCalledTwiceForSameEvent_fails() {
        val event = InstantEvent("myEvent", System.nanoTime())
        val sink = TestSink()
        val timeline = TimelineLogger(sink)

        timeline.recordEvent(event)

        assertThrows(IllegalStateException::class.java) {
            timeline.recordEvent(event)
        }
    }

    private class TestSink : TimelineLogSink {

        val writeEventStartCalled: MutableList<IntervalStartEvent> = ArrayList()
        val writeEventEndCalled: MutableList<IntervalEndEvent> = ArrayList()
        val writeEventCalled: MutableList<InstantEvent> = ArrayList()

        override fun publishEvent(event: Event) {
            when (event) {
                is InstantEvent -> {
                    writeEventCalled.add(event)
                }

                is IntervalStartEvent -> {
                    writeEventStartCalled.add(event)
                }

                is IntervalEndEvent -> {
                    writeEventEndCalled.add(event)
                }
            }
        }
    }
}