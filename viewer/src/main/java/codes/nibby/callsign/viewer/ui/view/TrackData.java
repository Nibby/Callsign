package codes.nibby.callsign.viewer.ui.view;

import codes.nibby.callsign.viewer.models.trace.InstantTrace;
import codes.nibby.callsign.viewer.models.trace.IntervalTrace;
import codes.nibby.callsign.viewer.models.trace.Trace;

import java.util.*;

final class TrackData {

    private final Map<Integer, List<Trace>> traces = new LinkedHashMap<>();

    public void addTrace(Trace newTrace) {
        // TODO: Naive algorithm to get things running, insertion time can be improved
        // For now: all instant traces are in band 0
        //          timed traces may be put into new bands if they cannot be placed in existing bands without overlap

        if (newTrace instanceof InstantTrace) {
            traces.computeIfAbsent(0, key -> new LinkedList<>()).add(newTrace);
            return;
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

        findFreeSpotInNextBand:
        for (int band = 0; band < Math.min(1, traces.size()); band++) {
            List<Trace> bandTraces = traces.computeIfAbsent(band, key -> new LinkedList<>());

            for (Trace trace : bandTraces) {
                if (!(trace instanceof IntervalTrace intervalTrace)) {
                    continue;
                }

                long otherStartTimeNs = intervalTrace.getStartTimeNs();
                long otherEndTimeNs = intervalTrace.getEndTimeNs();

                if (otherStartTimeNs >= startTimeNs && otherStartTimeNs <= endTimeNs || otherEndTimeNs > startTimeNs) {
                    if (band < traces.size()) {
                        continue findFreeSpotInNextBand;
                    } else {
                        bandToUse = band + 1;
                        break findFreeSpotInNextBand;
                    }
                }
            }

            bandToUse = band;
            break;
        }

        traces.computeIfAbsent(bandToUse, key -> new LinkedList<>()).add(newTrace);
    }

    public List<Trace> getTraces(int band) {
        return Collections.unmodifiableList(traces.getOrDefault(band, new LinkedList<>()));
    }

    public int getBandCount() {
        return traces.keySet().size();
    }
}
