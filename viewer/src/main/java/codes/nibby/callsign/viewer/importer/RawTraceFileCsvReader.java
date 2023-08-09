package codes.nibby.callsign.viewer.importer;

import codes.nibby.callsign.api.Event;
import codes.nibby.callsign.api.formats.CsvFormat;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public final class RawTraceFileCsvReader implements RawTraceFileReader {

    public final Path csvFile;

    public RawTraceFileCsvReader(Path csvFile) {
        this.csvFile = csvFile;
    }

    @Override
    public void streamEvents(Consumer<Event> eventConsumer) throws IOException {
        try (
            var reader = Files.newBufferedReader(this.csvFile);
            var csvReader = CsvFormat.Companion.createReader(reader)
        ) {
            csvReader.stream().forEach(csvRow -> {
                @Nullable var event = CsvFormat.Companion.deserialize(csvRow);

                if (event != null) {
                    eventConsumer.accept(event);
                }
            });
        }
    }
}
