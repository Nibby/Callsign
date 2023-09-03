package codes.nibby.callsign.viewer.ui.view;

final class TraceViewViewport {


    private double viewportWidth;
    private double viewportHeight;

    // Vertical display parameters
    // Note: track height is computed dynamically by TraceViewContentManager (and its related classes) depending on
    //       the number of overlapping traces in a single track.
    private double displayOffsetY = 0d;
    private double trackMargin = 5;
    private double trackMinimumHeight = 30d;

    // Horizontal display parameters
    private long earliestEventTimeNs;
    private long latestEventTimeNs;
    private long totalTimeRangeNs;

    private long displayStartTimeOffsetNs = 0;      // dictates horizontal scroll, leftmost edge of screen = earliestEventTimeNs + this
    private long displayTimeRangeNs = 1;            // viewportWidth expressed in timeNs terms
    private double trackHorizontalZoomFactor = 1d;

    private boolean firstCompute = true;

    private static final double EPSILON = 0.001d;

    public boolean applyProperties(double viewportWidth, double viewportHeight, long earliestEventTimeNs, long latestEventTimeNs) {
        // TODO: What should happen to zoom level after viewport width changes?

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

        return firstCompute || timeRangeChanged;
    }

    /**
     * Sets the zoom factor for the time axis. The zoom level is normalized, where 1d = 100% zoom,
     * which means the entire timeline fits within the viewport.
     *
     * @param zoomLevel Zoom level ({@code 0 < zoomLevel < ?}
     */
    public void setHorizontalZoom(double zoomLevel) {
        if (zoomLevel <= 0d) {
            throw new IllegalArgumentException("zoomLevel must be > 0d");
        }

        this.trackHorizontalZoomFactor = zoomLevel;

        double portionOfTimelineInView = 1d / trackHorizontalZoomFactor;
        this.displayTimeRangeNs = (long) Math.ceil(totalTimeRangeNs * portionOfTimelineInView);
    }

    /**
     * Converts time (in nanoseconds) to the displayed x-position on screen. If the time is less
     * than the earliest displayed time, returns -1. Or if the time is greater than the latest
     * displayed time, returns {@code getViewportWidth() + 1}.
     *
     * @param timeNs Time to convert to x-position on screen
     * @return x-position of the time
     */
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

    /**
     * Converts a display x-position to the corresponding time on the event timeline, accounting
     * for factors such as horizontal scroll offset and zoom factor.
     *
     * @param displayX The displayed x-position to convert
     * @return Time (in nanoseconds) represented on the x-position on screen
     */
    public long getTimeNsFromDisplayX(double displayX) {
        double portionOfViewport = displayX / viewportWidth;
        long portionOfDisplayTimeRange = Math.round(portionOfViewport * displayTimeRangeNs);

        return earliestEventTimeNs + displayStartTimeOffsetNs + portionOfDisplayTimeRange;
    }

    /**
     * @return Time (in nanoseconds) represented on the left edge of the viewable region.
     */
    public long getDisplayedEarliestEventTimeNs() {
        return getTimeNsFromDisplayX(0d);
    }

    /**
     * @return Time (in nanoseconds) represented on the right edge of the viewable region.
     */
    public long getDisplayedLatestEventTimeNs() {
        return getTimeNsFromDisplayX(getViewportWidth());
    }

    /**
     * @return Width (in pixels) of the viewable region on screen.
     */
    public double getViewportWidth() {
        return viewportWidth;
    }

    /**
     * @return Height (in pixels) of the viewable region on screen.
     */
    public double getViewportHeight() {
        return viewportHeight;
    }

    /**
     * @return Horizontal track zoom factor
     */
    public double getTrackHorizontalZoomFactor() {
        return trackHorizontalZoomFactor;
    }

    /**
     * The real height of a track is computed dynamically by {@link TraceViewTraceContentManager} and its related
     * classes depending on the number of overlapping interval traces.
     * <p/>
     * Method of this method is the bare minimum height a track must occupy.
     *
     * @return Minimum height, in pixels, of a track.
     */
    public double getTrackMinimumHeight() {
        return trackMinimumHeight;
    }

}
