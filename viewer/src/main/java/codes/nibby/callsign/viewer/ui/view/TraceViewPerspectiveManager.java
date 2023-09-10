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
    private long earliestEventTimeNs;
    private long latestEventTimeNs;
    private MajorTickIncrementDescriptor timelineMajorTickDescriptor;

    private long displayStartTimeOffsetNs = 0; // dictates horizontal scroll, leftmost edge of screen = earliestEventTimeNs + this value
    private HorizontalZoom trackHorizontalZoom = HorizontalZoom.of(1d);

    private boolean firstCompute = true;

    private static final double EPSILON = 0.001d;

    private static final DateFormat DATE_FORMAT_DD_MM_YY = new SimpleDateFormat("dd/MM/yyyy");
    private static final DateFormat TIME_FORMAT_HH_MM = new SimpleDateFormat("HH:mm");
    private static final DateFormat TIME_FORMAT_HH_MM_SS = new SimpleDateFormat("HH:mm:ss");
    private static final DateFormat TIME_FORMAT_HH_MM_SS_MILLI = new SimpleDateFormat("HH:mm:ss.SSS");

    private static final List<MajorTickIncrementType> SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS = new ArrayList<>();

    private record MajorTickIncrementType(TimeUnit unit, int amount, @Nullable DateFormat dateFormat, @Nullable DateFormat timeFormat) {
        public long getIncrementTimeNs() {
            return unit.toNanos(amount);
        }
    }

    static {
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.DAYS, 1, DATE_FORMAT_DD_MM_YY, TIME_FORMAT_HH_MM));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.DAYS, 2, DATE_FORMAT_DD_MM_YY, TIME_FORMAT_HH_MM));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.DAYS, 3, DATE_FORMAT_DD_MM_YY, TIME_FORMAT_HH_MM));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.DAYS, 4, DATE_FORMAT_DD_MM_YY, TIME_FORMAT_HH_MM));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.DAYS, 5, DATE_FORMAT_DD_MM_YY, TIME_FORMAT_HH_MM));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.DAYS, 6, DATE_FORMAT_DD_MM_YY, TIME_FORMAT_HH_MM));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.DAYS, 7, DATE_FORMAT_DD_MM_YY, TIME_FORMAT_HH_MM));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.DAYS, 14, DATE_FORMAT_DD_MM_YY, TIME_FORMAT_HH_MM));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.DAYS, 30, DATE_FORMAT_DD_MM_YY, TIME_FORMAT_HH_MM));

        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.HOURS, 1, null, TIME_FORMAT_HH_MM));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.HOURS, 2, null, TIME_FORMAT_HH_MM));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.HOURS, 3, null, TIME_FORMAT_HH_MM));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.HOURS, 4, null, TIME_FORMAT_HH_MM));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.HOURS, 5, null, TIME_FORMAT_HH_MM));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.HOURS, 6, DATE_FORMAT_DD_MM_YY, TIME_FORMAT_HH_MM));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.HOURS, 12, DATE_FORMAT_DD_MM_YY, TIME_FORMAT_HH_MM));

        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.MINUTES, 1, null, TIME_FORMAT_HH_MM_SS));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.MINUTES, 2, null, TIME_FORMAT_HH_MM_SS));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.MINUTES, 5, null, TIME_FORMAT_HH_MM_SS));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.MINUTES, 10, null, TIME_FORMAT_HH_MM));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.MINUTES, 20, null, TIME_FORMAT_HH_MM));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.MINUTES, 30, null, TIME_FORMAT_HH_MM));

        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.SECONDS, 1, null, TIME_FORMAT_HH_MM_SS));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.SECONDS, 2, null, TIME_FORMAT_HH_MM_SS));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.SECONDS, 5, null, TIME_FORMAT_HH_MM_SS));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.SECONDS, 10, null, TIME_FORMAT_HH_MM_SS));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.SECONDS, 20, null, TIME_FORMAT_HH_MM_SS));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.SECONDS, 30, null, TIME_FORMAT_HH_MM_SS));

        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.MILLISECONDS, 1, null, TIME_FORMAT_HH_MM_SS_MILLI));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.MILLISECONDS, 2, null, TIME_FORMAT_HH_MM_SS_MILLI));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.MILLISECONDS, 5, null, TIME_FORMAT_HH_MM_SS_MILLI));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.MILLISECONDS, 10, null, TIME_FORMAT_HH_MM_SS_MILLI));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.MILLISECONDS, 20, null, TIME_FORMAT_HH_MM_SS_MILLI));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.MILLISECONDS, 50, null, TIME_FORMAT_HH_MM_SS_MILLI));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.MILLISECONDS, 100, null, TIME_FORMAT_HH_MM_SS_MILLI));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.MILLISECONDS, 200, null, TIME_FORMAT_HH_MM_SS_MILLI));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.MILLISECONDS, 500, null, TIME_FORMAT_HH_MM_SS_MILLI));

        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.MICROSECONDS, 1, null, null));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.MICROSECONDS, 2, null, null));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.MICROSECONDS, 5, null, null));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.MICROSECONDS, 10, null, null));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.MICROSECONDS, 20, null, null));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.MICROSECONDS, 50, null, null));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.MICROSECONDS, 100, null, null));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.MICROSECONDS, 200, null, null));
        SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.add(new MajorTickIncrementType(TimeUnit.MICROSECONDS, 500, null, null));
    }

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

        if (firstCompute) {
            setZoom(HorizontalZoom.of(1d));
            this.firstCompute = false;
        }

        if (displayTimeRangeChanged) {
            double totalDisplayableTimeNs = trackHorizontalZoom.measureTimeNs(viewportWidth);
            this.timelineMajorTickDescriptor = computeTimelineMajorTickDescriptor(totalDisplayableTimeNs);
        }

        return firstCompute || totalTimeRangeChanged || displayTimeRangeChanged;
    }

    private MajorTickIncrementDescriptor computeTimelineMajorTickDescriptor(double displayedTimeRange) {
        Text measurement = new Text("XX:XX:XX.XXX");
        double width = measurement.getBoundsInLocal().getWidth();
        double minimumTimeNsIncrement = trackHorizontalZoom.measureTimeNs(width / 3 * 7);

        final int idealDisplayedTickCount = 8;

        int bestCandidateDisplayedTickCount = -1;
        MajorTickIncrementType bestCandidate = null;

        for (int i = SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.size() - 1; i >= 0; i--) {
            MajorTickIncrementType majorTickType = SUPPORTED_MAJOR_TICK_INCREMENT_COMBINATIONS.get(i);

            long unitTimeNs = majorTickType.getIncrementTimeNs();
            if (unitTimeNs < minimumTimeNsIncrement) {
                continue;
            }

            int displayedTickCount = (int) (Math.floor(displayedTimeRange / unitTimeNs));

            if (displayedTickCount < 4) {
                continue;
            } else if (bestCandidate == null || Math.abs(idealDisplayedTickCount - displayedTickCount) < bestCandidateDisplayedTickCount ){
                bestCandidate = majorTickType;
                bestCandidateDisplayedTickCount = displayedTickCount;
            }
        }

        if (bestCandidate != null) {
            long timeIncrement = Objects.requireNonNull(bestCandidate).unit.toNanos(bestCandidate.amount);
            return new MajorTickIncrementDescriptor(timeIncrement, bestCandidate.dateFormat, bestCandidate.timeFormat);
        } else {
            return new MajorTickIncrementDescriptor(Math.round(minimumTimeNsIncrement), null, null);
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
    public double getDisplayX(long timeNs) {
        long timeElapsedSinceDisplayStartTime = timeNs - (earliestEventTimeNs + displayStartTimeOffsetNs);
        double pixelsFromLeftEdge = trackHorizontalZoom.measurePixels(timeElapsedSinceDisplayStartTime);

        return gutterWidth + pixelsFromLeftEdge;
    }

    @Override
    public long getTimeNsFromDisplayX(double displayX) {
        long timeNsSinceDisplayStartTime = Math.round(trackHorizontalZoom.measureTimeNs(displayX));
        return earliestEventTimeNs + displayStartTimeOffsetNs + timeNsSinceDisplayStartTime;
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

    public void setDisplayOffsetTimeNs(long timeNs) {
        displayStartTimeOffsetNs = Math.max(0, timeNs);
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
