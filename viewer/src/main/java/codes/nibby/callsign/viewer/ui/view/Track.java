package codes.nibby.callsign.viewer.ui.view;

import codes.nibby.callsign.viewer.models.InstantTraceEvent;
import codes.nibby.callsign.viewer.models.TimedTraceEvent;
import codes.nibby.callsign.viewer.models.TraceEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

final class Track implements Comparable<Track> {

    private final String name;

    private Long earliestEntryTimeNs = null;
    private Long latestEntryEndTimeNs = null;

    public Track(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void notifyTraceAdded(TraceEvent event) {
        if (event instanceof InstantTraceEvent instantEvent) {
            earliestEntryTimeNs = (earliestEntryTimeNs == null)
                ? instantEvent.getTimeNs()
                : Math.min(instantEvent.getTimeNs(), earliestEntryTimeNs);

            latestEntryEndTimeNs = (latestEntryEndTimeNs == null)
                ? instantEvent.getTimeNs()
                : Math.max(instantEvent.getTimeNs(), latestEntryEndTimeNs);

        } else if (event instanceof TimedTraceEvent timedEvent) {
            earliestEntryTimeNs = (earliestEntryTimeNs == null)
                ? timedEvent.getStartTimeNs()
                : Math.min(timedEvent.getStartTimeNs(), earliestEntryTimeNs);

            latestEntryEndTimeNs = (latestEntryEndTimeNs == null)
                ? timedEvent.getStartTimeNs()
                : Math.max(timedEvent.getEndTimeNs(), latestEntryEndTimeNs);
        }
    }

    public Long getEarliestEntryTimeNs() {
        return earliestEntryTimeNs;
    }

    public Long getLatestEntryEndTimeNs() {
        return latestEntryEndTimeNs;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Track other)) {
            return false;
        }

        return Objects.equals(name, other.name);
    }

    @Override
    public int compareTo(@NotNull Track other) {
        if (this.earliestEntryTimeNs == null) {
            return 1;
        }

        if (other.earliestEntryTimeNs == null) {
            return -1;
        }

        return Long.compare(earliestEntryTimeNs, other.earliestEntryTimeNs);
    }
}
