package codes.nibby.callsign.viewer.models;

import java.util.Map;

public final class InstantTrace extends TraceEvent {

    private final long timeNs;

    public InstantTrace(Map<String, String> attributes, long timeNs) {
        super(attributes);
        this.timeNs = timeNs;
    }

    public long getTimeNs() {
        return timeNs;
    }
}
