package codes.nibby.callsign.viewer.models;

import java.io.IOException;

public interface TimelineDigestDocument {

    void load() throws IOException;

    void unload();

}
