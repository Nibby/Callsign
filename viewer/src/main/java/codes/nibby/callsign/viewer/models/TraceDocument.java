package codes.nibby.callsign.viewer.models;

import java.io.IOException;

public interface TraceDocument {

    void load() throws IOException;

    void unload();

    long getEarliestEventTimeNs();

    long getLatestEventTimeNs();

}
