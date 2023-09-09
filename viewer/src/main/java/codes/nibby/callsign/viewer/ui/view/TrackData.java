package codes.nibby.callsign.viewer.ui.view;

import codes.nibby.callsign.viewer.models.trace.InstantTrace;
import codes.nibby.callsign.viewer.models.trace.IntervalTrace;
import codes.nibby.callsign.viewer.models.trace.Trace;

import java.util.*;

final class TrackData {

    private final Map<Integer, List<Trace>> traces = new LinkedHashMap<>();

    public void addTrace(Trace newTrace) {
        if (newTrace instanceof InstantTrace) {
            traces.computeIfAbsent(0, key -> new LinkedList<>()).add(newTrace);
        } else if (newTrace instanceof IntervalTrace intervalTrace) {
            addIntervalTrace(intervalTrace);
        } else {
            throw new IllegalStateException("Unsupported trace type: " + newTrace.getClass());
        }
    }

    private void addIntervalTrace(IntervalTrace newTrace) {
        long startTimeNs = newTrace.getStartTimeNs();
        long endTimeNs = newTrace.getEndTimeNs();

        // Interval traces only
        int bandToUse = 0;

        // Avoid overlaps in interval traces by placing them into separate bands on the same track
        findFreeSpotInNextBand:
        do {
            List<Trace> bandTraces = traces.computeIfAbsent(bandToUse, key -> new LinkedList<>());

            for (Trace trace : bandTraces) {
                if (!(trace instanceof IntervalTrace intervalTrace)) {
                    continue;
                }

                long otherStartTimeNs = intervalTrace.getStartTimeNs();
                long otherEndTimeNs = intervalTrace.getEndTimeNs();

                if (otherStartTimeNs >= startTimeNs && otherStartTimeNs <= endTimeNs || otherEndTimeNs > startTimeNs) {
                    bandToUse++;
                    continue findFreeSpotInNextBand;
                }
            }

            break;

        } while (bandToUse < traces.size());

        traces.computeIfAbsent(bandToUse, key -> new LinkedList<>()).add(newTrace);
    }

    public List<Trace> getTraces(int band) {
        return Collections.unmodifiableList(traces.getOrDefault(band, new LinkedList<>()));
    }

    public int getBandCount() {
        return traces.keySet().size();
    }
}
