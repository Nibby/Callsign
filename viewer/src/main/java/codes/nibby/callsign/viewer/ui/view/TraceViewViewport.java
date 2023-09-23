package codes.nibby.callsign.viewer.ui.view;

import javafx.geometry.Rectangle2D;

import javax.annotation.Nullable;
import java.text.DateFormat;
import java.util.concurrent.TimeUnit;

/**
 * Specifies visual layout parameters for drawing content in trace view canvas. In addition, the
 * component provides API to translate on-screen position to logical time position
 * (accounting for horizontal zoom factor) and vice-versa.
 * <p/>
 * Visually, the trace view content area consists of zero or more tracks laid out horizontally in
 * some specified order. Each track has a header area to display metadata such as track name, as
 * well as a track content area to display traces for the track. On top of this, there are time
 * reference ticks drawn above the trace content area. This component manages the boundaries for
 * each major visual component.
 */
public interface TraceViewViewport {

    /**
     * This width value defines the area where the mouse can resize the
     * {@link #getTrackHeaderBounds() track header width} through dragging.
     *
     * @return Width, in pixels, of the divider between the track header area and viewport content.
     */
    double getTrackHeaderDividerWidth();

    /**
     * @return Bounds allocated to displaying track header area on the canvas.
     */
    Rectangle2D getTrackHeaderBounds();

    /**
     * @return Bounds allocated to displaying track content on the canvas.
     */
    Rectangle2D getTrackContentBounds();

    /**
     * @return Bounds allocated to displaying time ticker information on the canvas.
     */
    Rectangle2D getTimelineBounds();

    /**
     * The real height of a track is computed dynamically by {@link TraceViewTraceContentGenerator}
     * (and its related classes) depending on the number of overlapping interval traces. A track
     * consists of one or more bands, where each band contains enough space to display a single
     * trace.
     * <p/>
     * The thickness (or height) of a trace on screen is computed based on this number.
     *
     * @return Height of a single track band, in pixels.
     */
    double getTrackBandHeight();

    /**
     * Translates event time (in milliseconds) to the displayed x-position on screen.
     * <p/>
     * This method returns a pixel value relative to the {@link #getTrackContentBounds() track
     * content bounds}. In other words, if {@code timeMs} is the earliest displayable time on-screen,
     * this method returns {@code getTrackContentBounds().x}. Naturally, this method accounts for
     * the horizontal scroll offset.
     * <p/>
     * If the supplied time is less than the earliest displayed time on screen, this method returns
     * -1. Otherwise, if the time is greater than the maximum displayed time, returns
     * {@code getTrackContentBounds().width + 1}.
     *
     * @param timeMs Time to convert to x-position on screen.
     *
     * @return x-position of the time
     */
    double translateToTrackContentX(long timeMs);

    /**
     * @return Smallest cumulative track band index that is visible on-screen at the moment.
     */
    int getVisibleBandIndexStart();

    /**
     * @param totalBands Total number of track bands in the data
     * @return Largest cumulative track band index that is visible on-screen at the moment.
     */
    int getVisibleBandIndexEnd(int totalBands);

    /**
     * Translates a given y position in the viewport to a cumulative band index the position resides
     * within, accounting for current yOffset. If y position is not part of a valid band index,
     * returns -1.
     * <p/>
     * The result may exceed maximum displayable bands in the current data.
     *
     * @param yInViewport Y pixel position in the viewport.
     * @return Cumulative band index for the y position, or -1 if not available.
     */
    int translateToCumulativeBandIndex(double yInViewport);

    /**
     * Translates pixel x-position to its corresponding time on the event timeline, accounting
     * for factors such as horizontal scroll offset and zoom.
     *
     * @param trackContentX The displayed x-position to convert
     * @return Time (in milliseconds) represented on the x-position on screen
     */
    long translateToTimeMs(double trackContentX);

    /**
     * @return Current vertical scroll offset for track content, in pixels.
     */
    double getTrackContentOffsetY();

    /**
     * @param timeDurationMs Time duration to be measured.
     *
     * @return Time duration translated as a measurement of pixel width on screen, given the
     *         current horizontal zoom.
     */
    double measureDisplayedWidth(long timeDurationMs);

    /**
     * @return Parameters related to the display of time indicators on the trace content timeline.
     */
    TimelineDescriptor getTimelineDescriptor();

    /**
     * Check whether the given time instant is viewable under the current zoom and offset settings.
     *
     * @param timeMs Time, in milliseconds, to check visibility.
     * @return true if the time is visible on screen.
     */
    boolean isTimeMsVisible(long timeMs);

    /**
     * This number is smaller than {@link #getTrackBandHeight()} as the element needs to fit in the
     * band with extra padding.
     *
     * @return Height of the interval trace visual element, in pixels.
     */
    double getIntervalTraceHeight();

    /**
     * This number is smaller than {@link #getTrackBandHeight()} as the element needs to fit in the
     * band with extra padding.
     *
     * @return Square size of an instant trace visual element, in pixels.
     */
    double getInstantTraceSize();

    /**
     * Describes how major ticks should be displayed on the trace content timeline.
     *
     * @param amount Time interval value
     * @param unit Time unit
     * @param dateFormat Date display format
     * @param timeFormat Time display format
     */
    record TimelineDescriptor(
        int amount,
        TimeUnit unit,
        @Nullable DateFormat dateFormat,
        @Nullable DateFormat timeFormat
    ) {

        /**
         * @return Total increment time in milliseconds between each major tick
         */
        public long getIncrementTimeMs() {
            return unit.toMillis(amount);
        }
    }

}
