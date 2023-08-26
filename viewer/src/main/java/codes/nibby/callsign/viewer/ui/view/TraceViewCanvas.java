package codes.nibby.callsign.viewer.ui.view;

import codes.nibby.callsign.viewer.models.InstantTraceEvent;
import codes.nibby.callsign.viewer.models.TimedTraceEvent;
import codes.nibby.callsign.viewer.models.TraceEvent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import java.util.Map;

final class TraceViewCanvas extends Canvas {

    private final GraphicsContext graphics;

    public TraceViewCanvas() {
        this.graphics = getGraphicsContext2D();
    }

    public void paint(TraceViewViewport viewport, TraceEventCollection viewableEvents, TraceViewColorScheme colorScheme) {
        graphics.setFill(colorScheme.getBackground());
        graphics.fillRect(0, 0, viewport.getWidth(), viewport.getHeight());

        Map<Track, TrackData> trackData = viewableEvents.getTrackData();
        int trackIndex = 0;

        for (Track track : trackData.keySet()) {
            paintTrack(viewport, trackIndex, track, trackData.get(track), colorScheme);
            trackIndex++;
        }
    }

    private void paintTrack(TraceViewViewport viewport, int trackIndex, Track track, TrackData trackData, TraceViewColorScheme colorScheme) {
        double trackHeight = viewport.getTrackHeight();
        double yStart = viewport.getTrackStartDisplayY(trackIndex);

        for (TraceEvent trace : trackData.getTraces()) {

            if (trace instanceof TimedTraceEvent timedTrace) {

                // TODO: Oh no I dun goofed...
                if (timedTrace.getEndTimeNs() < 0) {
                    continue;
                }
                graphics.setFill(colorScheme.getTimedTraceEventBackground());

                double xStart = viewport.getDisplayX(timedTrace.getStartTimeNs());
                double xEnd = viewport.getDisplayX(timedTrace.getEndTimeNs());

                graphics.fillRect(xStart, yStart, xEnd, trackHeight);
            } else if (trace instanceof InstantTraceEvent instantTrace) {
                graphics.setFill(colorScheme.getInstantTraceEventBackground());

                double xStart = viewport.getDisplayX(instantTrace.getTimeNs());

                graphics.fillOval(xStart - 20, yStart + trackHeight / 2 - 20, 40, 40);
            }
        }
    }
}
