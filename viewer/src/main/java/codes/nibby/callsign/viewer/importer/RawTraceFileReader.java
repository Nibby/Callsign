package codes.nibby.callsign.viewer.importer;

import codes.nibby.callsign.api.Event;

import java.io.IOException;
import java.util.function.Consumer;

public interface RawTraceFileReader {

    void streamEvents(Consumer<Event> eventConsumer) throws IOException;

}
