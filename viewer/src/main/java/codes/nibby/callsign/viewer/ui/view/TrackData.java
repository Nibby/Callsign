package codes.nibby.callsign.viewer.ui.view;

import codes.nibby.callsign.viewer.models.TraceEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class TrackData {

    private final List<TraceEvent> traces = new ArrayList<>();

    private long earliestEntryTimeNs = Long.MAX_VALUE;

    public void addTrace(TraceEvent entry) {
        traces.add(entry);
    }

    public long getEarliestEntryTimeNs() {
        return earliestEntryTimeNs;
    }

    public List<TraceEvent> getTraces() {
        return Collections.unmodifiableList(traces);
    }
}
