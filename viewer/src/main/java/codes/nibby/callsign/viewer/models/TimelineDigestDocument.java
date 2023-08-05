package codes.nibby.callsign.viewer.models;

import java.io.IOException;

public interface TimelineDigestDocument {

    void loadForRead() throws IOException;

    void unload();

}
