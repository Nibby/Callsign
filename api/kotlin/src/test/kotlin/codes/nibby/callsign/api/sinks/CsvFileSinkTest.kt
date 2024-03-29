package codes.nibby.callsign.api.sinks

import codes.nibby.callsign.api.InstantEvent
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Instant
import java.util.*

class CsvFileSinkTest {

    private val testDataFolder = Paths.get(System.getProperty("user.dir")).resolve("test").resolve(generateRandomTestFolderName())

    private fun generateRandomTestFolderName(): String {
        return this.javaClass.name + "-" + UUID.randomUUID().toString()
    }

    @BeforeEach
    fun setup() {
        if (!Files.isDirectory(testDataFolder)) {
            Files.createDirectories(testDataFolder)
        }
    }

    @AfterEach
    fun teardown() {
        if (Files.isDirectory(testDataFolder)) {
            deleteFolderRecursive(testDataFolder)
        }
    }

    private fun deleteFolderRecursive(folder: Path) {
        val stream = Files.newDirectoryStream(folder)

        for (path in stream) {
            if (Files.isDirectory(path)) {
                deleteFolderRecursive(path)
            } else {
                Files.delete(path)
            }
        }

        Files.delete(folder)
    }

    @Test
    fun testConstructor_parentFoldersNotExist_createsThem() {
        val outputFolder = testDataFolder.resolve("notExistFolder").resolve("notExistFolder2")

        CsvFileSink(outputFolder.resolve("testFile"))

        Assertions.assertTrue(Files.isDirectory(outputFolder))
    }

    @Test
    fun testConstructor_outputFileMissing_createsIt() {
        val sink = CsvFileSink(testDataFolder.resolve("testFile"))

        Assertions.assertTrue(Files.exists(sink.outputFile))
    }

    @Test
    fun testConstructor_outputFileExists_preservesExistingContent() {
        val sink = CsvFileSink(testDataFolder.resolve("testFile"))
        sink.publishEvent(InstantEvent("Test", Instant.now().toEpochMilli()))

        val fileLengthInFirstSession = sink.outputFile.toFile().length()

        val sink2 = CsvFileSink(testDataFolder.resolve("testFile"))
        val fileLengthInSecondSession = sink2.outputFile.toFile().length()

        Assertions.assertTrue(Files.isSameFile(sink.outputFile, sink2.outputFile))
        Assertions.assertEquals(fileLengthInFirstSession, fileLengthInSecondSession)
    }

    @Test
    fun testPublishEvent_appendsDataToFileEveryTime() {
        val sink = CsvFileSink(testDataFolder.resolve("testFile"))

        testWritesDataEveryCall(sink) {
            sink.publishEvent(InstantEvent("Test", Instant.now().toEpochMilli()))
        }
    }

    private fun testWritesDataEveryCall(sink: CsvFileSink, iteration: Runnable) {
        var lengthBeforeWrite: Long = sink.outputFile.toFile().length()

        for (i in 0 .. 10) {
            iteration.run()

            val lengthAfterWrite = sink.outputFile.toFile().length()
            Assertions.assertTrue(lengthAfterWrite > lengthBeforeWrite, "$lengthBeforeWrite -> $lengthAfterWrite")

            lengthBeforeWrite = lengthAfterWrite
        }
    }
}