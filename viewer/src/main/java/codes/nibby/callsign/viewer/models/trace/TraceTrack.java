package codes.nibby.callsign.viewer.models.trace;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class TraceTrack implements Comparable<TraceTrack> {

    private final String binningAttributeName;
    private final String binningAttributeValue;
    private final String displayAttributeName;

    private Long earliestEntryTimeNs = null;
    private Long latestEntryEndTimeNs = null;

    public TraceTrack(
        String binningAttributeName,
        String binningAttributeValue,
        String displayAttributeName
    ) {
        this.binningAttributeName = binningAttributeName;
        this.binningAttributeValue = binningAttributeValue;
        this.displayAttributeName = displayAttributeName;
    }

    public String getBinningAttributeName() {
        return binningAttributeName;
    }

    public String getDisplayAttributeName() {
        return displayAttributeName;
    }

    public String getBinningAttributeValue() {
        return binningAttributeValue;
    }

    public void notifyTraceAdded(Trace event) {
        if (event instanceof InstantTrace instantEvent) {
            earliestEntryTimeNs = (earliestEntryTimeNs == null)
                ? instantEvent.getTimeNs()
                : Math.min(instantEvent.getTimeNs(), earliestEntryTimeNs);

            latestEntryEndTimeNs = (latestEntryEndTimeNs == null)
                ? instantEvent.getTimeNs()
                : Math.max(instantEvent.getTimeNs(), latestEntryEndTimeNs);

        } else if (event instanceof IntervalTrace timedEvent) {
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
        return Objects.hash(binningAttributeName, binningAttributeValue);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TraceTrack other)) {
            return false;
        }

        return Objects.equals(binningAttributeName, other.binningAttributeName)
            && Objects.equals(binningAttributeValue, other.binningAttributeValue);
    }

    @Override
    public int compareTo(@NotNull TraceTrack other) {
        if (this.earliestEntryTimeNs == null) {
            return 1;
        }

        if (other.earliestEntryTimeNs == null) {
            return -1;
        }

        return Long.compare(earliestEntryTimeNs, other.earliestEntryTimeNs);
    }
}
