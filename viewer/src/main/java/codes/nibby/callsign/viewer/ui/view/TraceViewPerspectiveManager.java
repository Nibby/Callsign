package codes.nibby.callsign.viewer.ui.view;

final class TraceViewPerspectiveManager implements TraceViewPerspective {

    private static final int HEIGHT_RESERVED_FOR_TIMELINE_DISPLAY = 45;

    private static final int MINIMUM_GUTTER_WIDTH = 100;

    private double gutterWidth = 200;

    private double viewportWidth;
    private double viewportHeight;

    // Vertical display parameters
    // Note: track height is computed dynamically by TraceViewContentManager (and its related classes) depending on
    //       the number of overlapping traces in a single track.
    private double displayOffsetY = 0d;
    private double trackBandHeight = 30d;

    // Horizontal display parameters
    private long earliestEventTimeNs;
    private long latestEventTimeNs;
    private long totalTimeRangeNs;

    private long displayStartTimeOffsetNs = 0;      // dictates horizontal scroll, leftmost edge of screen = earliestEventTimeNs + this
    private long displayTimeRangeNs = 1;            // viewportWidth expressed in timeNs terms
    private double trackHorizontalZoomFactor = 1d;

    private boolean firstCompute = true;

    private static final double EPSILON = 0.001d;

    /**
     * Update key properties required for computing viewable region, zoom and offset data.
     *
     * @param totalWidth Total amount of horizontal space, in pixels, to display trace viewer content
     * @param totalHeight Total amount of vertical space, in pixels, to display trace viewer content
     * @param earliestEventTimeNs Starting time of the earliest trace event, in nanoseconds
     * @param latestEventTimeNs Finish time of the latest trace event, in nanoseconds
     *
     * @return true if any perspective parameters have changed after calling this method. If true,
     *         the displayed content must be repainted so the new perspective is applied.
     */
    public boolean applyProperties(double totalWidth, double totalHeight, long earliestEventTimeNs, long latestEventTimeNs) {

        boolean totalTimeRangeChanged = false;
        boolean displayTimeRangeChanged = false;
        boolean firstCompute = this.firstCompute;

        if (Math.abs(this.viewportWidth - totalWidth) > EPSILON) {
            this.viewportWidth = totalWidth - gutterWidth;
            displayTimeRangeChanged = true;
        }

        if (Math.abs(this.viewportHeight - totalHeight) > EPSILON) {
            this.viewportHeight = totalHeight - HEIGHT_RESERVED_FOR_TIMELINE_DISPLAY;
            displayTimeRangeChanged = true;
        }

        if (this.earliestEventTimeNs != earliestEventTimeNs) {
            this.earliestEventTimeNs = earliestEventTimeNs;
            totalTimeRangeChanged = true;
        }

        if (this.latestEventTimeNs != latestEventTimeNs) {
            this.latestEventTimeNs = Math.max(latestEventTimeNs, this.earliestEventTimeNs + 1);
            totalTimeRangeChanged = true;
        }

        if (totalTimeRangeChanged) {
            this.totalTimeRangeNs = Math.max(1, this.latestEventTimeNs - this.earliestEventTimeNs);
        }

        if (firstCompute) {
            setHorizontalZoom(1d);
            this.firstCompute = false;
        }

        return firstCompute || totalTimeRangeChanged || displayTimeRangeChanged;
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

    @Override
    public double getDisplayX(long timeNs) {
        long timeElapsedSinceDisplayStartTime = timeNs - (earliestEventTimeNs + displayStartTimeOffsetNs);
        double portionOfViewableTimeRange = timeElapsedSinceDisplayStartTime / (double) displayTimeRangeNs;

        return gutterWidth + portionOfViewableTimeRange * viewportWidth;
    }

    @Override
    public long getTimeNsFromDisplayX(double displayX) {
        double portionOfViewport = Math.max((displayX - gutterWidth) / viewportWidth, 0);
        long portionOfDisplayTimeRange = Math.round(portionOfViewport * displayTimeRangeNs);

        return earliestEventTimeNs + portionOfDisplayTimeRange;
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

    @Override
    public double getGutterWidth() {
        return gutterWidth;
    }

    @Override
    public double getDividerSize() {
        return 10;
    }

    @Override
    public double getViewportStartY() {
        return HEIGHT_RESERVED_FOR_TIMELINE_DISPLAY;
    }

    @Override
    public double getViewportStartX() {
        return gutterWidth;
    }

    @Override
    public double getViewportWidth() {
        return viewportWidth;
    }

    @Override
    public double getViewportHeight() {
        return viewportHeight;
    }

    /**
     * @return Horizontal track zoom factor
     */
    public double getTrackHorizontalZoomFactor() {
        return trackHorizontalZoomFactor;
    }

    @Override
    public double getTrackBandHeight() {
        return trackBandHeight;
    }

    @Override
    public double getTimelineIndicatorHeight() {
        return HEIGHT_RESERVED_FOR_TIMELINE_DISPLAY;
    }

    public void setDisplayOffsetY(double displayOffsetY) {
        this.displayOffsetY = displayOffsetY;
    }

    public double getDisplayOffsetY() {
        return displayOffsetY;
    }

    public void setDisplayOffsetTimeNs(long timeNs) {
        displayStartTimeOffsetNs = Math.max(0, timeNs);
    }

    @Override
    public long getDisplayOffsetTimeNs() {
        return displayStartTimeOffsetNs;
    }

    @Override
    public double getDisplayWidth(long timeDurationNs) {
        return ((double) timeDurationNs / displayTimeRangeNs) * viewportWidth;
    }
}
