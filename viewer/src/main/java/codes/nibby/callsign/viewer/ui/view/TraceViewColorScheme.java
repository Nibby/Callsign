package codes.nibby.callsign.viewer.ui.view;

import javafx.scene.paint.Color;

public interface TraceViewColorScheme {

    Color getContentBackground();
    Color getContentRowBackground();
    Color getContentAlternateRowBackground();

    Color getGutterBackground();
    Color getGutterRowBackground();
    Color getGutterAlternateRowBackground();

    Color getInstantTraceEventBackground();
    Color getInstantTraceEventOutline();

    Color getIntervalTraceEventBackground();
    Color getIntervalTraceEventOutline();

    Color getSelectedTraceTimeInstanceMarker();
    Color getHoveredTraceTimeInstanceMarker();

    Color getTimelineBackground();
    Color getTimelineBorderBackground();
    Color getTimelineDescriptorTickInContentForeground();
    Color getTimelineDescriptorTick();
    Color getTimelineText();

}
