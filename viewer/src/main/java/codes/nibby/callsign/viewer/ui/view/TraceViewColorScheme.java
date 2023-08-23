package codes.nibby.callsign.viewer.ui.view;

import javafx.scene.paint.Color;

public interface TraceViewColorScheme {

    Color getBackgroundColor();

    Color getTimelineBaseForegroundColor();
    Color getTimelineMajorTickColor();
    Color getTimelineMinorTickColor();
    Color getTimelineTextColor();

}
