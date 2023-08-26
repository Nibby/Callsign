package codes.nibby.callsign.viewer.ui.view;

import javafx.scene.paint.Color;

public class TraceViewLightColorScheme implements TraceViewColorScheme {

    @Override
    public Color getBackground() {
        return Color.WHITE;
    }

    @Override
    public Color getInstantTraceEventBackground() {
        return Color.RED;
    }

    @Override
    public Color getTimedTraceEventBackground() {
        return Color.AQUA;
    }

    @Override
    public Color getTimelineBaseForeground() {
        return Color.LIGHTGRAY;
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

}
