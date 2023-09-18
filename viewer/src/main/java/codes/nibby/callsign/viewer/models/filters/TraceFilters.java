package codes.nibby.callsign.viewer.models.filters;

public final class TraceFilters {

    private long displayedEarliestTimeMs;
    private long displayedLatestTimeMs;

    public TraceFilters() {
    }

    public void setDisplayedTimeInterval(long earliestTimeMs, long latestTimeMs) {
        this.displayedEarliestTimeMs = earliestTimeMs;
        this.displayedLatestTimeMs = latestTimeMs;
    }

    public long getDisplayedEarliestTimeMs() {
        return displayedEarliestTimeMs;
    }

    public long getDisplayedLatestTimeMs() {
        return displayedLatestTimeMs;
    }
}
