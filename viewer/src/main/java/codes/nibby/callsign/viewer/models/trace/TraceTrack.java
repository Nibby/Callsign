package codes.nibby.callsign.viewer.models.trace;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class TraceTrack implements Comparable<TraceTrack> {

    private final String binningAttributeName;
    private final String binningAttributeValue;
    private final String displayAttributeName;

    private Long earliestEntryTimeMs = null;
    private Long latestEntryEndTimeMs = null;

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
            earliestEntryTimeMs = (earliestEntryTimeMs == null)
                ? instantEvent.getTimeMs()
                : Math.min(instantEvent.getTimeMs(), earliestEntryTimeMs);

            latestEntryEndTimeMs = (latestEntryEndTimeMs == null)
                ? instantEvent.getTimeMs()
                : Math.max(instantEvent.getTimeMs(), latestEntryEndTimeMs);

        } else if (event instanceof IntervalTrace timedEvent) {
            earliestEntryTimeMs = (earliestEntryTimeMs == null)
                ? timedEvent.getStartTimeMs()
                : Math.min(timedEvent.getStartTimeMs(), earliestEntryTimeMs);

            latestEntryEndTimeMs = (latestEntryEndTimeMs == null)
                ? timedEvent.getStartTimeMs()
                : Math.max(timedEvent.getEndTimeMs(), latestEntryEndTimeMs);
        }
    }

    public Long getEarliestEntryTimeMs() {
        return earliestEntryTimeMs;
    }

    public Long getLatestEntryEndTimeMs() {
        return latestEntryEndTimeMs;
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
        if (this.earliestEntryTimeMs == null) {
            return 1;
        }

        if (other.earliestEntryTimeMs == null) {
            return -1;
        }

        return Long.compare(earliestEntryTimeMs, other.earliestEntryTimeMs);
    }
}
