package codes.nibby.callsign.viewer.ui.view;

import javafx.scene.paint.Color;

public interface TraceViewColorScheme {

    Color getBackground();

    Color getInstantTraceEventBackground();
    Color getTimedTraceEventBackground();

    Color getTimelineBaseForeground();
    Color getTimelineMajorTick();
    Color getTimelineMinorTick();
    Color getTimelineText();

}
