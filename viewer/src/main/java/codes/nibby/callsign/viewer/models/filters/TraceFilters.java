package codes.nibby.callsign.viewer.models.filters;

public final class TraceFilters {

    private long displayedEarliestTimeNs;
    private long displayedLatestTimeNs;

    public TraceFilters() {
    }

    public void setDisplayedTimeInterval(long earliestTimeNs, long latestTimeNs) {
        this.displayedEarliestTimeNs = earliestTimeNs;
        this.displayedLatestTimeNs = latestTimeNs;
    }

    public long getDisplayedEarliestTimeNs() {
        return displayedEarliestTimeNs;
    }

    public long getDisplayedLatestTimeNs() {
        return displayedLatestTimeNs;
    }
}
