package codes.nibby.callsign.viewer.ui.view;

import codes.nibby.callsign.viewer.models.trace.Trace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class TrackData {

    private final List<Trace> traces = new ArrayList<>();

    private long earliestEntryTimeNs = Long.MAX_VALUE;

    public void addTrace(Trace entry) {
        traces.add(entry);
    }

    public long getEarliestEntryTimeNs() {
        return earliestEntryTimeNs;
    }

    public List<Trace> getTraces() {
        return Collections.unmodifiableList(traces);
    }
}
