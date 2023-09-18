package codes.nibby.callsign.api.formats

import codes.nibby.callsign.api.*
import codes.nibby.callsign.api.AttributeData
import de.siegmar.fastcsv.reader.CsvReader
import de.siegmar.fastcsv.writer.CsvWriter
import kotlinx.serialization.json.Json
import java.io.Reader
import java.io.Writer
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*

class CsvFormat {

    companion object {

        /** Callsign Raw Trace (file format) 1 */
        const val EXTENSION = "crt1"

        val CHARSET: Charset = StandardCharsets.UTF_8

        fun createWriter(writer: Writer): CsvWriter {
            return CsvWriter.builder().build(writer)
        }

        fun createReader(reader: Reader): CsvReader {
            return CsvReader.builder().build(reader)
        }

        fun serialize(event: Event): List<String> {
            val data = event.getAttributeData()
            val attributeData = Json.encodeToString(AttributeData.serializer(), data)

            return listOf(
                event.id.toString(),
                event.correlationId?.toString() ?: "",
                event.type,
                event.name,
                event.timeMs.toString(),
                attributeData
            )
        }

        fun deserialize(fields: List<String>): Event? {
            val expectedFields = 6

            if (fields.size != expectedFields) {
                return null
            }

            var index = 0

            val eventId = UUID.fromString(fields[index++])
            val correlationIdString = fields[index++]
            val eventType = fields[index++]
            val name = fields[index++]
            val timeMs = fields[index++].toLong()
            val attributeDataRaw = fields[index]

            val correlationId: UUID? = if (correlationIdString.isBlank()) null else UUID.fromString(correlationIdString)

            val event: Event = if (IntervalStartEvent.TYPE == eventType) {
                IntervalStartEvent(eventId, name, timeMs)
            } else if (IntervalEndEvent.TYPE == eventType) {
                IntervalEndEvent(eventId, correlationId!!, name, timeMs)
            } else if (InstantEvent.TYPE == eventType) {
                InstantEvent(eventId, name, timeMs)
            } else {
                return null
            }

            val attributeData = Json.decodeFromString<AttributeData>(attributeDataRaw)
            event.loadAttributeData(attributeData, includeSpecialAttributes = true)

            return event
        }
    }

}