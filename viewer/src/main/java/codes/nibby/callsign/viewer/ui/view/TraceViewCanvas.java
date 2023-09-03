package codes.nibby.callsign.viewer.ui.view;

import codes.nibby.callsign.viewer.models.trace.InstantTrace;
import codes.nibby.callsign.viewer.models.trace.IntervalTrace;
import codes.nibby.callsign.viewer.models.trace.Trace;
import codes.nibby.callsign.viewer.models.trace.TraceTrack;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class TraceViewCanvas extends Canvas {

    private final GraphicsContext graphics;

    public TraceViewCanvas() {
        this.graphics = getGraphicsContext2D();
    }

    public void paint(TraceViewViewport viewport, TraceCollection traces, TraceViewColorScheme colorScheme) {
        paintBackground(viewport, colorScheme);
        paintContent(viewport, traces, colorScheme);
    }

    private void paintBackground(TraceViewViewport viewport, TraceViewColorScheme colorScheme) {
        graphics.setFill(colorScheme.getBackground());
        graphics.fillRect(0, 0, viewport.getViewportWidth(), viewport.getViewportHeight());
    }

    private void paintContent(TraceViewViewport viewport, TraceCollection traces, TraceViewColorScheme colorScheme) {
        Map<TraceTrack, TrackData> trackDatum = traces.getTrackData();
        int trackIndex = 0;

        for (TraceTrack track : trackDatum.keySet()) {
            @Nullable TrackData trackData = trackDatum.get(track);

            if (trackData == null) {
                continue;
            }

            paintTraceTrack(viewport, trackIndex, track, trackData, colorScheme);

            trackIndex++;
        }
    }

    private void paintTraceTrack(TraceViewViewport viewport, int trackIndex, TraceTrack track, TrackData trackData, TraceViewColorScheme colorScheme) {
        double trackHeight = viewport.getTrackMinimumHeight();
        double yStart = trackIndex * trackHeight;

        graphics.setFill(trackIndex % 2 == 0 ? Color.ALICEBLUE : Color.WHITE);
        graphics.fillRect(0, yStart, viewport.getViewportWidth(), trackHeight);

        List<InstantTrace> instantTraces = new ArrayList<>();

        for (Trace trace : trackData.getTraces(0)) {
            if (trace instanceof IntervalTrace intervalTrace) {
                graphics.setFill(colorScheme.getTimedTraceEventBackground());

                double xStart = viewport.getDisplayX(intervalTrace.getStartTimeNs());
                double xEnd = viewport.getDisplayX(intervalTrace.getEndTimeNs());

                graphics.fillRect(xStart, yStart + 6, xEnd, trackHeight - 12);
            } else if (trace instanceof InstantTrace instantTrace) {
                instantTraces.add(instantTrace);
            }
        }

        for (InstantTrace instantTrace : instantTraces) {
            graphics.setFill(colorScheme.getInstantTraceEventBackground());

            double xStart = viewport.getDisplayX(instantTrace.getTimeNs());
            double size = trackHeight - 12;

            graphics.fillOval(xStart - size / 2, yStart + trackHeight / 2 - size / 2, size, size);
        }
    }
}
