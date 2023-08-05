package codes.nibby.callsign.api.sinks

import codes.nibby.callsign.api.InstantEvent
import codes.nibby.callsign.api.TimedEvent
import codes.nibby.callsign.api.TimelineLogSink
import codes.nibby.callsign.api.formats.CsvFormat
import de.siegmar.fastcsv.writer.CsvWriter
import java.io.BufferedWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

class CsvFileSink(outputFolder: Path, val nameWithoutExtension: String) : TimelineLogSink {

    internal val outputFile: Path

    private val csvWriter: CsvWriter
    private val bufferedWriter: BufferedWriter

    init {
        if (!Files.isDirectory((outputFolder))) {
            Files.createDirectories(outputFolder)
        }

        outputFile = outputFolder.resolve("$nameWithoutExtension.${CsvFormat.Extension}")

        if (!Files.exists(outputFile)) {
            Files.createFile(outputFile)
        }

        bufferedWriter = Files.newBufferedWriter(outputFile, CsvFormat.Charset, StandardOpenOption.APPEND)
        csvWriter = CsvFormat.createWriter(bufferedWriter)
    }

    override fun writeEventStart(event: TimedEvent) {
        val data = CsvFormat.serialize(event)
        csvWriter.writeRow(data)
        bufferedWriter.flush()
    }

    override fun writeEventEnd(event: TimedEvent) {
        val data = CsvFormat.serialize(event)
        csvWriter.writeRow(data)
        bufferedWriter.flush()
    }

    override fun writeEvent(event: InstantEvent) {
        val data = CsvFormat.serialize(event)
        csvWriter.writeRow(data)
        bufferedWriter.flush()
    }

}