package codes.nibby.callsign.viewer.models;

import java.util.Map;

public final class InstantTraceEntry extends TraceEntry {

    private final long timeNs;

    public InstantTraceEntry(String name, Map<String, String> attributes, long timeNs) {
        super(name, attributes);
        this.timeNs = timeNs;
    }

    public long getTimeNs() {
        return timeNs;
    }
}
