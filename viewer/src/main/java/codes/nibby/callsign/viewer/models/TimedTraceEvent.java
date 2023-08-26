package codes.nibby.callsign.viewer.models;

import java.util.Map;

public final class TimedTraceEvent extends TraceEvent {

    private final long startTimeNs;
    private final long endTimeNs;

    public TimedTraceEvent(Map<String, String> attributes, long startTimeNs, long endTimeNs) {
        super(attributes);

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
