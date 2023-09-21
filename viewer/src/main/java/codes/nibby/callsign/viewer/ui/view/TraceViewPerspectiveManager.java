package codes.nibby.callsign.viewer.ui.view;

import javafx.scene.text.Text;

import javax.annotation.Nullable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

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
    private static final double TRACK_BAND_HEIGHT = 30d;

    // Horizontal display parameters
    private long earliestEventTimeMs;
    private long latestEventTimeMs;
    private MajorTickIncrementDescriptor timelineMajorTickDescriptor;

    private long displayStartTimeOffsetMs = 0; // dictates horizontal scroll, leftmost edge of screen = earliestEventTimeMs + this value
    private HorizontalZoom trackHorizontalZoom = HorizontalZoom.of(1d);

    private boolean firstCompute = true;

    private static final double EPSILON = 0.001d;

    private static final DateFormat DATE_FORMAT_DD_MM_YY = new SimpleDateFormat("dd/MM/yyyy");
    private static final DateFormat TIME_FORMAT_HH_MM = new SimpleDateFormat("HH:mm");
    private static final DateFormat TIME_FORMAT_HH_MM_SS = new SimpleDateFormat("HH:mm:ss");
    private static final DateFormat TIME_FORMAT_HH_MM_SS_MILLI = new SimpleDateFormat("HH:mm:ss.SSS");

    private static final List<MajorTickIncrementType> SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS = new ArrayList<>();

    private record MajorTickIncrementType(TimeUnit unit, int amount, @Nullable DateFormat dateFormat, @Nullable DateFormat timeFormat) {
        public long getIncrementTimeMs() {
            return unit.toMillis(amount);
        }
    }

    static {
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.DAYS, 30, DATE_FORMAT_DD_MM_YY, TIME_FORMAT_HH_MM));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.DAYS, 14, DATE_FORMAT_DD_MM_YY, TIME_FORMAT_HH_MM));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.DAYS, 7, DATE_FORMAT_DD_MM_YY, TIME_FORMAT_HH_MM));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.DAYS, 6, DATE_FORMAT_DD_MM_YY, TIME_FORMAT_HH_MM));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.DAYS, 5, DATE_FORMAT_DD_MM_YY, TIME_FORMAT_HH_MM));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.DAYS, 4, DATE_FORMAT_DD_MM_YY, TIME_FORMAT_HH_MM));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.DAYS, 3, DATE_FORMAT_DD_MM_YY, TIME_FORMAT_HH_MM));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.DAYS, 2, DATE_FORMAT_DD_MM_YY, TIME_FORMAT_HH_MM));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.DAYS, 1, DATE_FORMAT_DD_MM_YY, TIME_FORMAT_HH_MM));

        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.HOURS, 12, DATE_FORMAT_DD_MM_YY, TIME_FORMAT_HH_MM));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.HOURS, 6, DATE_FORMAT_DD_MM_YY, TIME_FORMAT_HH_MM));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.HOURS, 5, null, TIME_FORMAT_HH_MM));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.HOURS, 4, null, TIME_FORMAT_HH_MM));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.HOURS, 3, null, TIME_FORMAT_HH_MM));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.HOURS, 2, null, TIME_FORMAT_HH_MM));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.HOURS, 1, null, TIME_FORMAT_HH_MM));

        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.MINUTES, 30, null, TIME_FORMAT_HH_MM));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.MINUTES, 20, null, TIME_FORMAT_HH_MM));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.MINUTES, 10, null, TIME_FORMAT_HH_MM));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.MINUTES, 5, null, TIME_FORMAT_HH_MM_SS));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.MINUTES, 2, null, TIME_FORMAT_HH_MM_SS));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.MINUTES, 1, null, TIME_FORMAT_HH_MM_SS));

        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.SECONDS, 30, null, TIME_FORMAT_HH_MM_SS));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.SECONDS, 20, null, TIME_FORMAT_HH_MM_SS));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.SECONDS, 10, null, TIME_FORMAT_HH_MM_SS));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.SECONDS, 5, null, TIME_FORMAT_HH_MM_SS));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.SECONDS, 2, null, TIME_FORMAT_HH_MM_SS));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.SECONDS, 1, null, TIME_FORMAT_HH_MM_SS));

        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.MILLISECONDS, 500, null, TIME_FORMAT_HH_MM_SS_MILLI));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.MILLISECONDS, 200, null, TIME_FORMAT_HH_MM_SS_MILLI));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.MILLISECONDS, 100, null, TIME_FORMAT_HH_MM_SS_MILLI));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.MILLISECONDS, 50, null, TIME_FORMAT_HH_MM_SS_MILLI));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.MILLISECONDS, 20, null, TIME_FORMAT_HH_MM_SS_MILLI));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.MILLISECONDS, 10, null, TIME_FORMAT_HH_MM_SS_MILLI));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.MILLISECONDS, 5, null, TIME_FORMAT_HH_MM_SS_MILLI));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.MILLISECONDS, 2, null, TIME_FORMAT_HH_MM_SS_MILLI));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.MILLISECONDS, 1, null, TIME_FORMAT_HH_MM_SS_MILLI));
    }

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

        if (displayTimeRangeChanged) {
            this.timelineMajorTickDescriptor = computeTimelineMajorTickDescriptor();
        }

        return firstCompute || totalTimeRangeChanged || displayTimeRangeChanged;
    }

    private MajorTickIncrementDescriptor computeTimelineMajorTickDescriptor() {
        Text measurement = new Text("XX:XX:XX.XXX");
        double width = measurement.getBoundsInLocal().getWidth();
        double minimumTimeMsIncrement = trackHorizontalZoom.measureTimeMs(width / 3 * 4);

        MajorTickIncrementType bestCandidate = null;

        for (int i = SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.size() - 1; i >= 0; i--) {
            MajorTickIncrementType majorTickType = SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.get(i);

            long unitTimeMs = majorTickType.getIncrementTimeMs();
            if (unitTimeMs < minimumTimeMsIncrement) {
                continue;
            }

            bestCandidate = majorTickType;
            break;
        }

        if (bestCandidate != null) {
            long timeIncrementMs = Objects.requireNonNull(bestCandidate).unit.toMillis(bestCandidate.amount);
            return new MajorTickIncrementDescriptor(timeIncrementMs, bestCandidate.dateFormat, bestCandidate.timeFormat);
        } else {
            return new MajorTickIncrementDescriptor(Math.round(minimumTimeMsIncrement), null, null);
        }
    }

    /**
     * Sets the zoom factor for the time axis. The zoom level is normalized, where 1d = 10_000 µs per pixel.
     *
     * @param zoom New zoom level
     */
    public void setZoom(HorizontalZoom zoom) {
        this.trackHorizontalZoom = Objects.requireNonNull(zoom);
    }

    @Override
    public double getDisplayX(long timeMs) {
        long timeElapsedSinceDisplayStartTime = timeMs - (earliestEventTimeMs + displayStartTimeOffsetMs);
        double pixelsFromLeftEdge = trackHorizontalZoom.measurePixels(timeElapsedSinceDisplayStartTime);

        return gutterWidth + pixelsFromLeftEdge;
    }

    @Override
    public long getTimeMsFromDisplayX(double displayX) {
        long timeMsSinceDisplayStartTime = Math.round(trackHorizontalZoom.measureTimeMs(displayX));
        return earliestEventTimeMs + displayStartTimeOffsetMs + timeMsSinceDisplayStartTime;
    }

    /**
     * @return Time (in milliseconds) represented on the left edge of the viewable region.
     */
    public long getDisplayedEarliestEventTimeMs() {
        return getTimeMsFromDisplayX(0d);
    }

    /**
     * @return Time (in milliseconds) represented on the right edge of the viewable region.
     */
    public long getDisplayedLatestEventTimeMs() {
        return getTimeMsFromDisplayX(getViewportWidth());
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
    public HorizontalZoom getTrackHorizontalZoom() {
        return trackHorizontalZoom;
    }

    @Override
    public double getTrackBandHeight() {
        return TRACK_BAND_HEIGHT;
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

    public void setDisplayOffsetTimeMs(long timeMs) {
        displayStartTimeOffsetMs = Math.max(0, timeMs);
    }

    @Override
    public double getDisplayWidth(long timeDurationNs) {
        return trackHorizontalZoom.measurePixels(timeDurationNs);
    }

    @Override
    public MajorTickIncrementDescriptor getTimelineMajorTickDescriptor() {
        return timelineMajorTickDescriptor;
    }
}
