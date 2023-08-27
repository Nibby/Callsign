package codes.nibby.callsign.viewer.models;

import codes.nibby.callsign.api.Event;

import java.io.IOException;

public interface WritableTraceDocument extends TraceDocument {

    void initialize() throws IOException;

    void appendEvent(Event event) throws IOException;

}
