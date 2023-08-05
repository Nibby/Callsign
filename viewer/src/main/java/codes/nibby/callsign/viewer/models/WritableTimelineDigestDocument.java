package codes.nibby.callsign.viewer.models;

import codes.nibby.callsign.api.Event;

import java.io.IOException;

public interface WritableTimelineDigestDocument extends TimelineDigestDocument {

    void loadForWrite() throws IOException;

    void appendEvent(Event event);

}
