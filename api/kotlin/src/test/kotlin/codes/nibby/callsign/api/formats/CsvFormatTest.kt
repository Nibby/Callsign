package codes.nibby.callsign.api.formats

import codes.nibby.callsign.api.AttributeData
import codes.nibby.callsign.api.InstantEvent
import codes.nibby.callsign.api.IntervalEndEvent
import codes.nibby.callsign.api.IntervalStartEvent
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CsvFormatTest {

    @Test
    fun testSerialize_instantEvent() {
        val time = Instant.now().toEpochMilli()
        val id = UUID.randomUUID()
        val eventName = "TestEvent"
        val event = InstantEvent(id, eventName, time)
        event.putAttribute("testAttribute", "testValue")
        event.putAttribute("testAttribute2", "testValue2")

        val attributeData = Json.encodeToString(AttributeData.serializer(), event.getAttributeData())

        val data = CsvFormat.serialize(event)

        assertDataEquals(
            data,
            id.toString(),
            "",
            InstantEvent.TYPE,
            eventName,
            time.toString(),
            attributeData
        )
    }

    @Test
    fun testSerialize_intervalStartEvent() {
        val time = Instant.now().toEpochMilli()
        val id = UUID.randomUUID()
        val eventName = "TestEvent"
        val event = IntervalStartEvent(id, eventName, time)
        event.putAttribute("testAttribute", "testValue")
        event.putAttribute("testAttribute2", "testValue2")

        val attributeData = Json.encodeToString(AttributeData.serializer(), event.getAttributeData())

        val data = CsvFormat.serialize(event)

        assertDataEquals(
            data,
            id.toString(),
            "",
            IntervalStartEvent.TYPE,
            eventName,
            time.toString(),
            attributeData
        )
    }

    @Test
    fun testSerialize_intervalEndEvent() {
        val time = Instant.now().toEpochMilli()
        val id = UUID.randomUUID()
        val correlationId = UUID.randomUUID()
        val eventName = "TestEvent"
        val event = IntervalEndEvent(id, correlationId, eventName, time)
        event.putAttribute("testAttribute", "testValue")
        event.putAttribute("testAttribute2", "testValue2")

        val attributeData = Json.encodeToString(AttributeData.serializer(), event.getAttributeData())


        val data = CsvFormat.serialize(event)

        assertDataEquals(
            data,
            id.toString(),
            correlationId.toString(),
            IntervalEndEvent.TYPE,
            eventName,
            time.toString(),
            attributeData
        )
    }

    @Test
    fun testRoundTripDeserialize_instantEvent() {
        val time = Instant.now().toEpochMilli()
        val id = UUID.randomUUID()
        val eventName = "TestEvent"
        val event = IntervalStartEvent(id, eventName, time)
        event.putAttribute("testAttribute", "testValue")
        event.putAttribute("testAttribute2", "testValue2")

        val data = CsvFormat.serialize(event)

        val deserializedEvent = CsvFormat.deserialize(data)

        assertNotNull(deserializedEvent)
        assertIs<IntervalStartEvent>(deserializedEvent)
        assertEquals(id, deserializedEvent.id)
        assertEquals(null, deserializedEvent.correlationId)
        assertEquals(eventName, deserializedEvent.name)
        assertEquals(time, deserializedEvent.timeMs)
        assertEquals(event.getAttributeData(), deserializedEvent.getAttributeData())
    }

    @Test
    fun testRoundTripDeserialize_intervalStartEvent() {
        val time = Instant.now().toEpochMilli()
        val id = UUID.randomUUID()
        val eventName = "TestEvent"
        val event = IntervalStartEvent(id, eventName, time)
        event.putAttribute("testAttribute", "testValue")
        event.putAttribute("testAttribute2", "testValue2")

        val data = CsvFormat.serialize(event)

        val deserializedEvent = CsvFormat.deserialize(data)

        assertNotNull(deserializedEvent)
        assertIs<IntervalStartEvent>(deserializedEvent)
        assertEquals(id, deserializedEvent.id)
        assertEquals(null, deserializedEvent.correlationId)
        assertEquals(eventName, deserializedEvent.name)
        assertEquals(time, deserializedEvent.timeMs)
        assertEquals(event.getAttributeData(), deserializedEvent.getAttributeData())
    }

    @Test
    fun testRoundTripDeserialize_intervalEndEvent() {
        val time = Instant.now().toEpochMilli()
        val id = UUID.randomUUID()
        val correlationId = UUID.randomUUID()
        val eventName = "TestEvent"
        val event = IntervalEndEvent(id, correlationId, eventName, time)
        event.putAttribute("testAttribute", "testValue")
        event.putAttribute("testAttribute2", "testValue2")

        val data = CsvFormat.serialize(event)

        val deserializedEvent = CsvFormat.deserialize(data)

        assertNotNull(deserializedEvent)
        assertIs<IntervalEndEvent>(deserializedEvent)
        assertEquals(id, deserializedEvent.id)
        assertEquals(correlationId, deserializedEvent.correlationId)
        assertEquals(eventName, deserializedEvent.name)
        assertEquals(time, deserializedEvent.timeMs)
        assertEquals(event.getAttributeData(), deserializedEvent.getAttributeData())
    }

    @Test
    fun testDeserialize_emptyFields_returnsNull() {
        assertNull(CsvFormat.deserialize(listOf()))
    }

    private fun assertDataEquals(data: List<String>, vararg dataParts: String) {
        assertEquals(dataParts.size, data.size, message = "Serialized data length does not match expected")

        for (i in data.indices) {
            assertEquals(dataParts[i], data[i])
        }
    }
}