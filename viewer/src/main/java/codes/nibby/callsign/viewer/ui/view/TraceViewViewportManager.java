package codes.nibby.callsign.viewer.ui.view;

import javafx.geometry.Rectangle2D;
import javafx.scene.text.Text;

import java.util.Objects;

final class TraceViewViewportManager implements TraceViewViewport {

    private static final int TIMELINE_HEIGHT = 45;
    private static final int MINIMUM_TRACK_HEADER_WIDTH = 100;
    private static final int TRACK_HEADER_DIVIDER_WIDTH = 10;

    private static final double DEFAULT_TRACK_HEADER_WIDTH = 200;

    private Rectangle2D timelineBounds = new Rectangle2D(0, 0, 0, 0);
    private Rectangle2D trackHeaderBounds = new Rectangle2D(0, 0, 0, 0);
    private Rectangle2D trackContentBounds = new Rectangle2D(0, 0, 0, 0);

    // Vertical display parameters
    // Note: track height is computed dynamically by TraceViewContentManager (and its related classes) depending on
    //       the number of overlapping traces in a single track.
    private double trackContentOffsetY = 0d;
    private static final double TRACK_BAND_HEIGHT = 30d;

    // Horizontal display parameters
    private long earliestEventTimeMs;
    private long latestEventTimeMs;
    private TimelineDescriptor timelineMajorTickDescriptor;

    private long trackContentTimeOffsetMs = 0; // dictates horizontal scroll, leftmost edge of screen = earliestEventTimeMs + this value
    private HorizontalZoom trackHorizontalZoom = HorizontalZoom.of(1d);

    private boolean firstCompute = true;

    private static final double EPSILON = 0.001d;

    /**
     * Update key properties required for computing viewable region, zoom and offset data.
     *
     * @param totalWidth Total amount of horizontal space, in pixels, to display trace viewer content
     * @param totalHeight Total amount of vertical space, in pixels, to display trace viewer content
     * @param earliestEventTimeMs Starting time of the earliest trace event, in milliseconds
     * @param latestEventTimeMs Finish time of the latest trace event, in milliseconds
     *
     * @return true if any perspective parameters have changed after calling this method. If true,
     *         the displayed content must be repainted so the new perspective is applied.
     */
    public boolean applyProperties(double totalWidth, double totalHeight, long earliestEventTimeMs, long latestEventTimeMs) {

        boolean totalTimeRangeChanged = false;
        boolean trackContentBoundsChanged = false;
        boolean firstCompute = this.firstCompute;

        double newTrackContentWidth = trackContentBounds.getWidth();
        double newTrackContentHeight = trackContentBounds.getHeight();

        if (firstCompute) {
           timelineBounds = new Rectangle2D(0, 0, Math.max(0, totalWidth), TIMELINE_HEIGHT);

           final double trackContentHeight = Math.max(0, totalHeight - TIMELINE_HEIGHT);

           trackHeaderBounds = new Rectangle2D(0, TIMELINE_HEIGHT, DEFAULT_TRACK_HEADER_WIDTH, trackContentHeight);

           trackContentBounds = new Rectangle2D(
               trackHeaderBounds.getWidth(),
               TIMELINE_HEIGHT,
               Math.max(0, totalWidth - trackHeaderBounds.getWidth()),
               trackContentHeight
           );
        }

        if (Math.abs(trackContentBounds.getWidth() - (totalWidth - DEFAULT_TRACK_HEADER_WIDTH)) > EPSILON) {
            newTrackContentWidth = Math.max(1, totalWidth - DEFAULT_TRACK_HEADER_WIDTH);
            trackContentBoundsChanged = true;
        }

        if (Math.abs(trackContentBounds.getHeight() - (totalHeight - TIMELINE_HEIGHT)) > EPSILON) {
            newTrackContentHeight = Math.max(1, totalHeight - TIMELINE_HEIGHT);

            trackHeaderBounds = new Rectangle2D(
                trackHeaderBounds.getMinX(),
                trackHeaderBounds.getMinY(),
                trackHeaderBounds.getWidth(),
                newTrackContentHeight
            );

            trackContentBoundsChanged = true;
        }

        if (trackContentBoundsChanged) {
            trackContentBounds = new Rectangle2D(
                trackContentBounds.getMinX(),
                trackContentBounds.getMinY(),
                newTrackContentWidth,
                newTrackContentHeight
            );
        }

        if (this.earliestEventTimeMs != earliestEventTimeMs) {
            this.earliestEventTimeMs = earliestEventTimeMs;
            totalTimeRangeChanged = true;
        }

        if (this.latestEventTimeMs != latestEventTimeMs) {
            this.latestEventTimeMs = Math.max(latestEventTimeMs, this.earliestEventTimeMs + 1);
            totalTimeRangeChanged = true;
        }

        if (firstCompute) {
            setZoom(HorizontalZoom.of(1d));
            this.firstCompute = false;
        }

        return firstCompute || totalTimeRangeChanged || trackContentBoundsChanged;
    }

    private TimelineDescriptor computeTimelineMajorTickDescriptor() {
        // TODO: Cleaner way to abstract this
        Text measurement = new Text("XX:XX:XX.XXX");
        double width = measurement.getBoundsInLocal().getWidth();
        double minimumTimeMsIncrement = trackHorizontalZoom.measureTimeMs(width / 3 * 4);

        for (int i = TimelineDescriptors.PREFERRED_VALUES.size() - 1; i >= 0; i--) {
            TimelineDescriptor majorTickType = TimelineDescriptors.PREFERRED_VALUES.get(i);

            if (majorTickType.getIncrementTimeMs() < minimumTimeMsIncrement) {
                continue;
            }

            return majorTickType;
        }

        return TimelineDescriptors.LAST_RESORT;
    }

    /**
     * Sets the zoom factor for the time axis. The zoom level is normalized, where 1d = 10_000 µs per pixel.
     *
     * @param zoom New zoom level
     */
    public void setZoom(HorizontalZoom zoom) {
        this.trackHorizontalZoom = Objects.requireNonNull(zoom);
        this.timelineMajorTickDescriptor = computeTimelineMajorTickDescriptor();
    }

    @Override
    public double translateToTrackContentX(long timeMs) {
        long timeElapsedSinceDisplayStartTime = timeMs - (earliestEventTimeMs + trackContentTimeOffsetMs);
        double pixelsFromLeftEdge = trackHorizontalZoom.measurePixels(timeElapsedSinceDisplayStartTime);

        return DEFAULT_TRACK_HEADER_WIDTH + pixelsFromLeftEdge;
    }

    @Override
    public int getVisibleBandIndexStart() {
        return Math.max(0, (int) Math.floor(getTrackContentOffsetY() / getTrackBandHeight()) - 2);
    }

    @Override
    public int getVisibleBandIndexEnd(int totalBands) {
        return Math.min(
            totalBands,
            getVisibleBandIndexStart() + (int) Math.ceil((trackContentBounds.getHeight() + timelineBounds.getHeight()) / getTrackBandHeight()) + 2
        );
    }

    @Override
    public int translateToCumulativeBandIndex(double yInViewport) {
        double yInContent = yInViewport - trackContentBounds.getMinY();
        double yWithOffset = yInContent + trackContentOffsetY;

        int bandIndex = (int) Math.floor(yWithOffset / getTrackBandHeight());

        return bandIndex >= 0 ? bandIndex : -1;
    }

    @Override
    public long translateToTimeMs(double trackContentX) {
        long timeMsSinceDisplayedEarliestStartTime = Math.round(trackHorizontalZoom.measureTimeMs(trackContentX));
        return earliestEventTimeMs + trackContentTimeOffsetMs + timeMsSinceDisplayedEarliestStartTime;
    }

    @Override
    public double measureTimeMs(double widthInPixels) {
        return trackHorizontalZoom.measureTimeMs(widthInPixels);
    }

    /**
     * @return Time (in milliseconds) represented on the left edge of the viewable region.
     */
    public long getDisplayedEarliestEventTimeMs() {
        return translateToTimeMs(0d);
    }

    /**
     * @return Time (in milliseconds) represented on the right edge of the viewable region.
     */
    public long getDisplayedLatestEventTimeMs() {
        return translateToTimeMs(trackContentBounds.getWidth());
    }

    @Override
    public double getTrackHeaderDividerWidth() {
        return TRACK_HEADER_DIVIDER_WIDTH;
    }

    @Override
    public Rectangle2D getTrackHeaderBounds() {
        return trackHeaderBounds;
    }

    @Override
    public Rectangle2D getTrackContentBounds() {
        return trackContentBounds;
    }

    @Override
    public Rectangle2D getTimelineBounds() {
        return timelineBounds;
    }

    /**
     * @return Horizontal track zoom factor
     */
    public HorizontalZoom getTrackHorizontalZoom() {
        return trackHorizontalZoom;
    }

    @Override
    public double getTrackBandHeight() {
        return TRACK_BAND_HEIGHT;
    }

    public void setTrackContentOffsetY(double offsetY) {
        this.trackContentOffsetY = offsetY;
    }

    public double getTrackContentOffsetY() {
        return trackContentOffsetY;
    }

    public void setTrackContentTimeOffsetMs(long timeMs) {
        trackContentTimeOffsetMs = Math.max(0, timeMs);
    }

    @Override
    public double measureDisplayedWidth(long timeDurationMs) {
        return trackHorizontalZoom.measurePixels(timeDurationMs);
    }

    @Override
    public TimelineDescriptor getTimelineDescriptor() {
        return timelineMajorTickDescriptor;
    }

    @Override
    public boolean isTimeMsVisible(long timeMs) {
        return timeMs >= getDisplayedEarliestEventTimeMs() && timeMs <= getDisplayedLatestEventTimeMs();
    }

    @Override
    public double getIntervalTraceHeight() {
        return Math.max(0, getTrackBandHeight() - 8);
    }

    @Override
    public double getInstantTraceSize() {
        return Math.max(0, getTrackBandHeight() - 16);
    }
}
