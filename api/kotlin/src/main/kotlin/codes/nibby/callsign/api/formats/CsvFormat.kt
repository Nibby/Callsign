package codes.nibby.callsign.api.formats

import codes.nibby.callsign.api.AttributeData
import codes.nibby.callsign.api.InstantEvent
import codes.nibby.callsign.api.TimedEvent
import de.siegmar.fastcsv.reader.CsvReader
import de.siegmar.fastcsv.writer.CsvWriter
import kotlinx.serialization.json.Json
import java.io.Reader
import java.io.Writer
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

class CsvFormat {

    companion object {

        val CHARSET: Charset = StandardCharsets.UTF_8
        val EXTENSION = "csff1"

        private val TYPE_TIMED_EVENT = "t"
        private val TYPE_INSTANT_EVENT = "i"

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
                TYPE_TIMED_EVENT,
                event.name,
                if (event.startTimeNs != null) event.startTimeNs.toString() else "-1",
                if (event.endTimeNs != null) event.endTimeNs.toString() else "-1",
                attributeData
            )
        }

        fun serialize(event: InstantEvent): List<String> {
            val data = event.getAttributeData()
            val attributeData = Json.encodeToString(AttributeData.serializer(), data)

            return listOf(
                TYPE_INSTANT_EVENT,
                event.name,
                "",
                "",
                attributeData
            )
        }
    }

}