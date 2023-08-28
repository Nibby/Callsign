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
class CsvFileSink(outputFileWithoutExtension: Path) : TimelineLogSink {

    internal val outputFile: Path

    private val csvWriter: CsvWriter
    private val bufferedWriter: BufferedWriter

    init {
        val parentFolder = outputFileWithoutExtension.parent

        if (!Files.isDirectory((parentFolder))) {
            Files.createDirectories(parentFolder)
        }

        val fileName = outputFileWithoutExtension.fileName.toString()
        val realFileName = "$fileName.${CsvFormat.EXTENSION}"
        outputFile = parentFolder.resolve(realFileName)

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