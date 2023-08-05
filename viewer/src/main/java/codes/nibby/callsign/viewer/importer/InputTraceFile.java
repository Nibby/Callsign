package codes.nibby.callsign.viewer.importer;

import codes.nibby.callsign.api.Event;
import codes.nibby.callsign.api.formats.CsvFormat;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

public final class InputTraceFile {

    private static final Map<String, Class<? extends InputTraceFileCsvFormatReader>> FILE_EXTENSION_READERS = new HashMap<>();
    private static final List<String> SUPPORTED_FILE_EXTENSIONS;

    static {
        FILE_EXTENSION_READERS.put(CsvFormat.Companion.getExtension(), InputTraceFileCsvFormatReader.class);

        SUPPORTED_FILE_EXTENSIONS = new ArrayList<>(FILE_EXTENSION_READERS.keySet());
    }

    public final Path path;
    private final String extension;

    public InputTraceFile(Path path) {
        this.extension = assertIsSupportedFileExtension(path);
        this.path = path;
    }

    private String assertIsSupportedFileExtension(Path path) {
        String fileName = path.getFileName().toString();

        for (String extension : SUPPORTED_FILE_EXTENSIONS) {
            if (fileName.endsWith("." + extension)) {
                return extension;
            }
        }

        throw new IllegalArgumentException("Unsupported input file extension: " + fileName);
    }

    public void streamEventData(Consumer<Event> eventConsumer) throws IOException {
        InputTraceFileFormatReader reader = createReader();
        reader.streamEvents(eventConsumer);
    }

    private InputTraceFileFormatReader createReader() {
        Class<? extends InputTraceFileCsvFormatReader> readerClass = FILE_EXTENSION_READERS.get(this.extension);

        if (readerClass == null) {
            throw new IllegalStateException("No reader class mapped for extension: " + this.extension);
        }

        try {
            Constructor<? extends InputTraceFileCsvFormatReader> constructor = readerClass.getConstructor(Path.class);
            return constructor.newInstance(this.path);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> getSupportedFileExtensions() {
        return Collections.unmodifiableList(SUPPORTED_FILE_EXTENSIONS);
    }
}
