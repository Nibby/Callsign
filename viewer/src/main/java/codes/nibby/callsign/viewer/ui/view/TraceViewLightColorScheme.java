package codes.nibby.callsign.viewer.ui.view;

import javafx.scene.paint.Color;

public class TraceViewLightColorScheme implements TraceViewColorScheme {

    @Override
    public Color getBackgroundColor() {
        return Color.WHITE;
    }

    @Override
    public Color getTimelineBaseForegroundColor() {
        return Color.LIGHTGRAY;
    }

    @Override
    public Color getTimelineMajorTickColor() {
        return Color.GRAY;
    }

    @Override
    public Color getTimelineMinorTickColor() {
        return Color.LIGHTGRAY;
    }

    @Override
    public Color getTimelineTextColor() {
        return Color.DARKGRAY;
    }

}
