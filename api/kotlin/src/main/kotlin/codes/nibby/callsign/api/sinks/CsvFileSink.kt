package codes.nibby.callsign.api.sinks

import codes.nibby.callsign.api.*
import codes.nibby.callsign.api.formats.CsvFormat
import de.siegmar.fastcsv.writer.CsvWriter
import java.io.BufferedWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

/**
 * A [TimelineLogger] sink that persists events to a CSV format file on disk.
 */
class CsvFileSink(outputFolder: Path, nameWithoutExtension: String) : TimelineLogSink {

    internal val outputFile: Path

    private val csvWriter: CsvWriter
    private val bufferedWriter: BufferedWriter

    init {
        if (!Files.isDirectory((outputFolder))) {
            Files.createDirectories(outputFolder)
        }

        outputFile = outputFolder.resolve("$nameWithoutExtension.${CsvFormat.EXTENSION}")

        if (!Files.exists(outputFile)) {
            Files.createFile(outputFile)
        }

        bufferedWriter = Files.newBufferedWriter(outputFile, CsvFormat.CHARSET, StandardOpenOption.APPEND)
        csvWriter = CsvFormat.createWriter(bufferedWriter)
    }

    override fun publishEvent(event: Event) {
        val data = CsvFormat.serialize(event)
        csvWriter.writeRow(data)
        bufferedWriter.flush()
    }

}