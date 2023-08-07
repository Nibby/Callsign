package codes.nibby.callsign.viewer.models;

import codes.nibby.callsign.api.Event;

import java.io.IOException;

public interface WritableTimelineDigestDocument extends TimelineDigestDocument {

    void appendEvent(Event event) throws IOException;

}
