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
class CsvFileSink(outputFile: Path) : TimelineLogSink {

    private val realOutputFile: Path

    private val csvWriter: CsvWriter
    private val bufferedWriter: BufferedWriter

    init {
        val parentFolder = outputFile.parent

        if (!Files.isDirectory((parentFolder))) {
            Files.createDirectories(parentFolder)
        }

        val fileName = outputFile.fileName.toString()
        val realFileName = "$fileName.${CsvFormat.EXTENSION}"
        realOutputFile = parentFolder.resolve(realFileName)

        if (!Files.exists(realOutputFile)) {
            Files.createFile(realOutputFile)
        }

        bufferedWriter = Files.newBufferedWriter(realOutputFile, CsvFormat.CHARSET, StandardOpenOption.APPEND)
        csvWriter = CsvFormat.createWriter(bufferedWriter)
    }

    override fun publishEvent(event: Event) {
        val data = CsvFormat.serialize(event)
        csvWriter.writeRow(data)
        bufferedWriter.flush()
    }

}