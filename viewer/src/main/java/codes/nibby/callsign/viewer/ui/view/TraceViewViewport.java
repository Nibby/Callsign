package codes.nibby.callsign.viewer.ui.view;

final class TraceViewViewport {

    private double viewportWidth;
    private double viewportHeight;

    private double viewOffsetY = 0d;
    private double timeOffsetNs = 0d; // horizontal scroll offset

    private double trackHeight = 40;
    private double trackMargin = 5;

    private long earliestEventTimeNs;
    private long latestEventTimeNs;
    private long totalTimeRangeNs;
    private double trackHorizontalZoom = 1d;
    private boolean firstCompute = true;

    private long displayStartTimeOffsetNs = 0;
    private long displayTimeRangeNs = 1;

    private static final double EPSILON = 0.001d;

    public void recompute(double viewportWidth, double viewportHeight, long earliestEventTimeNs, long latestEventTimeNs) {
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;

        boolean timeRangeChanged = false;

        if (this.earliestEventTimeNs != earliestEventTimeNs) {
            this.earliestEventTimeNs = earliestEventTimeNs;
            timeRangeChanged = true;
        }

        if (this.latestEventTimeNs != latestEventTimeNs) {
            this.latestEventTimeNs = Math.max(latestEventTimeNs, this.earliestEventTimeNs + 1);
            timeRangeChanged = true;
        }

        if (timeRangeChanged) {
            this.totalTimeRangeNs = Math.max(1, this.latestEventTimeNs - this.earliestEventTimeNs);
        }

        if (firstCompute) {
            setHorizontalZoom(1d);
        }
    }

    // normalized zoom level where 1d = entire timeline is viewable
    public void setHorizontalZoom(double zoomLevel) {
        if (zoomLevel <= 0d) {
            throw new IllegalArgumentException("zoomLevel must be > 0d");
        }

        this.trackHorizontalZoom = zoomLevel;

        double portionOfTimelineInView = 1d / trackHorizontalZoom;
        this.displayTimeRangeNs = (long) Math.ceil(totalTimeRangeNs * portionOfTimelineInView);
    }

    public double getTrackStartDisplayY(int trackIndex) {
        if (trackIndex < 0) {
            throw new IllegalArgumentException("trackIndex must be >= 0");
        }

        return trackIndex * (trackHeight + trackMargin) + viewOffsetY;
    }

    public double getDisplayX(long timeNs) {

        long timeElapsedSinceDisplayOffset = timeNs - displayStartTimeOffsetNs - earliestEventTimeNs;

        if (timeElapsedSinceDisplayOffset < 0) {
            return -1; // exceeds left screen bounds, turn into constant
        }

        double portionOfViewableTimeRange = timeElapsedSinceDisplayOffset / (double) displayTimeRangeNs;

        if (portionOfViewableTimeRange > 1d) {
            return viewportWidth + 1;
        }

        return portionOfViewableTimeRange * viewportWidth;
    }

    public long getTimeFromDisplayX(double displayX) {
        double portionOfViewport = displayX / viewportWidth;
        long portionOfDisplayTimeRange = (long) Math.round(portionOfViewport * displayTimeRangeNs);

        return earliestEventTimeNs + displayStartTimeOffsetNs + portionOfDisplayTimeRange;
    }

    public double getWidth() {
        return viewportWidth;
    }

    public double getHeight() {
        return viewportHeight;
    }

    public double getTrackHeight() {
        return trackHeight;
    }
}
