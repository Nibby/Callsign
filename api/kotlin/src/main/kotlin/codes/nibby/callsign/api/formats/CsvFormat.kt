package codes.nibby.callsign.api.formats

import codes.nibby.callsign.api.*
import codes.nibby.callsign.api.AttributeData
import de.siegmar.fastcsv.reader.CsvReader
import de.siegmar.fastcsv.reader.CsvRow
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
                event.type,
                event.name,
                event.correlationId?.toString() ?: "",
                event.timeNs.toString(),
                attributeData
            )
        }

        fun deserialize(csvRow: CsvRow): Event? {
            var index = 0

            val eventType = csvRow.getField(index++)
            val name = csvRow.getField(index++)
            val correlationIdString = csvRow.getField(index++)
            val timeNs = csvRow.getField(index++).toLong()
            val attributeDataRaw = csvRow.getField(index)

            val correlationId: UUID? = if (correlationIdString.isBlank()) null else UUID.fromString(correlationIdString)

            val event: Event

            if (IntervalStartEvent.TYPE == eventType) {
                event = IntervalStartEvent(name, timeNs)
            } else if (IntervalEndEvent.TYPE == eventType) {
                event = IntervalEndEvent(name, timeNs, correlationId!!)
            } else if (InstantEvent.TYPE == eventType) {
                event = InstantEvent(name, timeNs)
            } else {
                return null
            }

            val attributeData = Json.decodeFromString<AttributeData>(attributeDataRaw)
            event.loadAttributeData(attributeData, includeSpecialAttributes = true)

            return event
        }
    }

}