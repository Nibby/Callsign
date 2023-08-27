package codes.nibby.callsign.api

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

class TimelineLoggerTest {

    @Test
    fun testRecordEventStart_returnedEventStartTimeSet() {
        val timeline = TimelineLogger(TestSink())

        val timedEvent = timeline.recordEventStart("timed event 1")

        Assertions.assertNotNull(timedEvent.timeNs)
    }

    // Not intended to test for time resolution, just checking the recorded time is
    // not completely bogus.
    @Test
    fun testRecordEventStart_startTimeNsRoughlyCorrect() {
        val timeline = TimelineLogger(TestSink())

        val timedEvent = timeline.recordEventStart("timed event 1")
        val timeNow = System.nanoTime()

        val nsDifference = timeNow - timedEvent.timeNs
        val nsTolerance = TimeUnit.MILLISECONDS.toNanos(100)

        Assertions.assertTrue(
            nsDifference <= nsTolerance,
            "nsDifference too large! Got: $nsDifference (expected < $nsTolerance)"
        )
    }

    @Test
    fun testRecordEventStart_nameSet() {
        val timeline = TimelineLogger(TestSink())
        val name = "timed event 1"

        val timedEvent = timeline.recordEventStart(name)

        Assertions.assertEquals(name, timedEvent.name)
    }

    @Test
    fun testRecordEventStart_invokesSinkWriterMethod() {
        val sink = TestSink()
        val timeline = TimelineLogger(sink)

        val timedEvent = timeline.recordEventStart("timed event 1")

        Assertions.assertTrue(sink.writeEventStartCalled.contains(timedEvent))
    }

    @Test
    fun testRecordEventEnd_endTimeSet() {
        val timeline = TimelineLogger(TestSink())
        val timedEvent = timeline.recordEventStart("timed event 1")

        timeline.recordEventEnd(timedEvent)

        Assertions.assertNotNull(timedEvent)
    }

    // Not intended to test for time resolution, just checking the recorded time is
    // not completely bogus.
    @Test
    fun testRecordEventEnd_endTimeRoughlyCorrect() {
        val timeline = TimelineLogger(TestSink())
        val timedEvent = timeline.recordEventStart("timed event 1")

        timeline.recordEventEnd(timedEvent)

        val timeNow = System.nanoTime()
        val nsDifference = timeNow - timedEvent.timeNs
        val nsTolerance = TimeUnit.MILLISECONDS.toNanos(100)

        Assertions.assertTrue(
            nsDifference <= nsTolerance,
            "nsDifference too large! Got: $nsDifference (expected < $nsTolerance)"
        )
    }

    @Test
    fun testRecordEventEnd_methodCalledTwiceForSameEvent_fails() {
        val timeline = TimelineLogger(TestSink())
        val timedEvent = timeline.recordEventStart("timed event 1")

        Assertions.assertDoesNotThrow {
            timeline.recordEventEnd(timedEvent)
        }

        Assertions.assertThrows(IllegalStateException::class.java) {
            timeline.recordEventEnd(timedEvent)
        }
    }

    @Test
    fun testRecordEventEnd_marksEventAsSaved() {
        val timeline = TimelineLogger(TestSink())
        val timedEvent = timeline.recordEventStart("timed event 1")

        timeline.recordEventEnd(timedEvent)

        Assertions.assertTrue(timedEvent.saved)
    }

    @Test
    fun testRecordEvent_marksEventAsSaved() {
        val event = InstantEvent("myEvent")
        val timeline = TimelineLogger(TestSink())

        timeline.recordEvent(event)

        Assertions.assertTrue(event.saved)
    }

    @Test
    fun testRecordEvent_invokesSinkWriterMethod() {
        val event = InstantEvent("myEvent")
        val sink = TestSink()
        val timeline = TimelineLogger(sink)

        timeline.recordEvent(event)

        Assertions.assertTrue(sink.writeEventCalled.contains(event))
    }

    @Test
    fun testRecordEvent_methodCalledTwiceForSameEvent_fails() {
        val event = InstantEvent("myEvent")
        val sink = TestSink()
        val timeline = TimelineLogger(sink)

        timeline.recordEvent(event)

        Assertions.assertThrows(IllegalStateException::class.java) {
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