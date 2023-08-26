package codes.nibby.callsign.api.formats

import codes.nibby.callsign.api.AttributeData
import codes.nibby.callsign.api.Event
import codes.nibby.callsign.api.InstantEvent
import codes.nibby.callsign.api.TimedEvent
import de.siegmar.fastcsv.reader.CsvReader
import de.siegmar.fastcsv.reader.CsvRow
import de.siegmar.fastcsv.writer.CsvWriter
import kotlinx.serialization.json.Json
import java.io.Reader
import java.io.Writer
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

class CsvFormat {

    companion object {

        val Charset: Charset = StandardCharsets.UTF_8
        val Extension = "csff1"

        fun createWriter(writer: Writer): CsvWriter {
            return CsvWriter.builder().build(writer)
        }

        fun createReader(reader: Reader): CsvReader {
            return CsvReader.builder().build(reader)
        }

        fun serialize(event: TimedEvent): List<String> {
            val data = event.getAttributeData()
            val attributeData = Json.encodeToString(AttributeData.serializer(), data)

            return listOf(
                event.type,
                event.getName(),
                if (event.startTimeNs != null) event.startTimeNs.toString() else Long.MAX_VALUE.toString(),
                if (event.endTimeNs != null) event.endTimeNs.toString() else Long.MIN_VALUE.toString(),
                attributeData
            )
        }

        fun serialize(event: InstantEvent): List<String> {
            val data = event.getAttributeData()
            val attributeData = Json.encodeToString(AttributeData.serializer(), data)

            return listOf(
                event.type,
                event.getName(),
                event.timeNs.toString(),
                "",
                attributeData
            )
        }

        fun deserialize(csvRow: CsvRow): Event? {
            val eventType = csvRow.getField(0)
            val name = csvRow.getField(1)
            val startTimeRaw = csvRow.getField(2)
            val endTimeRaw = csvRow.getField(3)
            val attributeDataRaw = csvRow.getField(4)

            val event: Event

            if (TimedEvent.TYPE.equals(eventType)) {
                val timedEvent = TimedEvent(name, startTimeRaw.toLongOrNull())
                timedEvent.endTimeNs = endTimeRaw.toLongOrNull()

                event = timedEvent
            } else if (InstantEvent.TYPE.equals(eventType)) {
                event = InstantEvent(name, startTimeRaw.toLong())
            } else {
                return null
            }

            val attributeData = Json.decodeFromString<AttributeData>(attributeDataRaw)

            for (entry in attributeData.map.entries) {
                event.putAttribute(entry.key, entry.value)
            }

            return event
        }
    }

}