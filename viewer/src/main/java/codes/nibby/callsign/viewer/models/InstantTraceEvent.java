package codes.nibby.callsign.viewer.models;

import java.util.Map;

public final class InstantTraceEvent extends TraceEvent {

    private final long timeNs;

    public InstantTraceEvent(Map<String, String> attributes, long timeNs) {
        super(attributes);
        this.timeNs = timeNs;
    }

    public long getTimeNs() {
        return timeNs;
    }
}
