package codes.nibby.callsign.viewer.ui.view;

public interface TraceViewPerspective {

    double getGutterWidth();

    double getDividerSize();

    double getViewportStartY();

    double getViewportStartX();

    /**
     * @return Width (in pixels) of the viewable region on screen.
     */
    double getViewportWidth();

    /**
     * @return Height (in pixels) of the viewable region on screen.
     */
    double getViewportHeight();

    /**
     * The real height of a track is computed dynamically by {@link TraceViewTraceContentManager} and its related
     * classes depending on the number of overlapping interval traces.
     * <p/>
     * Method of this method is the bare minimum height a track must occupy.
     *
     * @return Minimum height, in pixels, of a track.
     */
    double getTrackBandHeight();

    /**
     *
     * @return Height, in pixels, of the component painting timeline markers
     */
    double getTimelineIndicatorHeight();

    /**
     * Converts time (in nanoseconds) to the displayed x-position on screen. If the time is less
     * than the earliest displayed time, returns -1. Or if the time is greater than the latest
     * displayed time, returns {@code getViewportWidth() + 1}.
     *
     * @param timeNs Time to convert to x-position on screen
     * @return x-position of the time
     */
    double getDisplayX(long timeNs);

    /**
     * Converts a display x-position to the corresponding time on the event timeline, accounting
     * for factors such as horizontal scroll offset and zoom factor.
     *
     * @param displayX The displayed x-position to convert
     * @return Time (in nanoseconds) represented on the x-position on screen
     */
    long getTimeNsFromDisplayX(double displayX);

    double getDisplayOffsetY();

    long getDisplayOffsetTimeNs();

    double getDisplayWidth(long timeDurationNs);
}
