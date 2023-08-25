package codes.nibby.callsign.viewer.models;

import java.util.Map;

public final class TimedTraceEntry extends TraceEntry {

    private final long startTimeNs;
    private final long endTimeNs;

    public TimedTraceEntry(String name, Map<String, String> attributes, long startTimeNs, long endTimeNs) {
        super(name, attributes);

        this.startTimeNs = startTimeNs;
        this.endTimeNs = endTimeNs;
    }

    public long getStartTimeNs() {
        return startTimeNs;
    }

    public long getEndTimeNs() {
        return endTimeNs;
    }
}
