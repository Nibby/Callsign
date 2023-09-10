package codes.nibby.callsign.viewer.ui.view;

import javafx.scene.paint.Color;

public final class TraceViewLightColorScheme implements TraceViewColorScheme {

    @Override
    public Color getContentBackground() {
        return Color.LIGHTGRAY;
    }

    @Override
    public Color getContentRowBackground() {
        return Color.WHITE;
    }

    private static final Color CONTENT_ALTERNATE_ROW_BACKGROUND = new Color(0.98d, 0.98d, 0.98d, 1d);

    @Override
    public Color getContentAlternateRowBackground() {
        return CONTENT_ALTERNATE_ROW_BACKGROUND;
    }

    private static final Color GUTTER_BACKGROUND = new Color(0.5d, 0.5d, 0.5d, 1d);

    @Override
    public Color getGutterBackground() {
        return GUTTER_BACKGROUND;
    }

    private static final Color GUTTER_ROW_BACKGROUND = new Color(0.4d, 0.4d, 0.4d, 1d);

    @Override
    public Color getGutterRowBackground() {
        return GUTTER_ROW_BACKGROUND;
    }

    private static final Color GUTTER_ALTERNATE_ROW_BACKGROUND = new Color(0.32d, 0.32d, 0.32d, 1d);

    @Override
    public Color getGutterAlternateRowBackground() {
        return GUTTER_ALTERNATE_ROW_BACKGROUND;
    }

    private static final Color INSTANT_TRACE_EVENT_BACKGROUND = new Color(255 / 255d, 183 / 255d, 0 / 255d, 1d);

    @Override
    public Color getInstantTraceEventBackground() {
        return INSTANT_TRACE_EVENT_BACKGROUND;
    }

    private static final Color INSTANT_TRACE_EVENT_OUTLINE = new Color(250 / 255d, 131 / 255d, 26 / 255d, 1d);

    @Override
    public Color getInstantTraceEventOutline() {
        return INSTANT_TRACE_EVENT_OUTLINE;
    }

    private static final Color INTERVAL_TRACE_EVENT_BACKGROUND = new Color(200 / 255d, 238 / 255d, 255 / 255d, 1d);

    @Override
    public Color getIntervalTraceEventBackground() {
        return INTERVAL_TRACE_EVENT_BACKGROUND;
    }

    private static final Color INTERVAL_TRACE_EVENT_OUTLINE = new Color(90 / 255d, 160 / 255d, 220 / 255d, 1d);

    @Override
    public Color getIntervalTraceEventOutline() {
        return INTERVAL_TRACE_EVENT_OUTLINE;
    }

    private static final Color TIMELINE_INDICATOR_LINES_IN_CONTENT_FOREGROUND = Color.color(0.92d, 0.92d, 0.92d, 1d);

    @Override
    public Color getTimelineIndicatorLinesInContentForeground() {
        return TIMELINE_INDICATOR_LINES_IN_CONTENT_FOREGROUND;
    }

    @Override
    public Color getTimelineMajorTick() {
        return Color.GRAY;
    }

    @Override
    public Color getTimelineMinorTick() {
        return Color.LIGHTGRAY;
    }

    @Override
    public Color getTimelineText() {
        return Color.DARKGRAY;
    }

    private static final Color TIMELINE_BACKGROUND = new Color(0.95d, 0.95d, 0.95d, 1d);

    @Override
    public Color getTimelineBackground() {
        return TIMELINE_BACKGROUND;
    }

    @Override
    public Color getTimelineBorderBackground() {
        return Color.LIGHTGRAY;
    }

}
