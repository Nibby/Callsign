package codes.nibby.callsign.viewer.ui.view;

import codes.nibby.callsign.viewer.models.trace.InstantTrace;
import codes.nibby.callsign.viewer.models.trace.IntervalTrace;
import codes.nibby.callsign.viewer.models.trace.Trace;

import java.util.*;

final class TrackData {

    private final Map<Integer, Set<Trace>> traces = new LinkedHashMap<>();

    public void addTrace(Trace newTrace) {
        if (newTrace instanceof InstantTrace) {
            traces.computeIfAbsent(0, key -> new LinkedHashSet<>()).add(newTrace);
        } else if (newTrace instanceof IntervalTrace intervalTrace) {
            addIntervalTrace(intervalTrace);
        } else {
            throw new IllegalStateException("Unsupported trace type: " + newTrace.getClass());
        }
    }

    private void addIntervalTrace(IntervalTrace newTrace) {
        long startTimeMs = newTrace.getStartTimeMs();
        long endTimeMs = newTrace.getEndTimeMs();

        // Interval traces only
        int bandToUse = 0;

        // Avoid overlaps in interval traces by placing them into separate bands on the same track
        findFreeSpotInNextBand:
        do {
            Set<Trace> bandTraces = traces.computeIfAbsent(bandToUse, key -> new LinkedHashSet<>());

            for (Trace trace : bandTraces) {
                if (!(trace instanceof IntervalTrace intervalTrace)) {
                    continue;
                }

                long otherStartTimeMs = intervalTrace.getStartTimeMs();
                long otherEndTimeMs = intervalTrace.getEndTimeMs();

                if (otherStartTimeMs >= startTimeMs && otherStartTimeMs <= endTimeMs || otherEndTimeMs > startTimeMs) {
                    bandToUse++;
                    continue findFreeSpotInNextBand;
                }
            }

            break;

        } while (bandToUse < traces.size());

        traces.computeIfAbsent(bandToUse, key -> new LinkedHashSet<>()).add(newTrace);
    }

    public Set<Trace> getTraces(int band) {
        var value = traces.getOrDefault(band, new LinkedHashSet<>());
        return Collections.unmodifiableSet(value);
    }

    public int getBandCount() {
        return traces.keySet().size();
    }
}
