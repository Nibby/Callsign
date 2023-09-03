package codes.nibby.callsign.viewer.ui.view;

import codes.nibby.callsign.viewer.models.trace.InstantTrace;
import codes.nibby.callsign.viewer.models.trace.IntervalTrace;
import codes.nibby.callsign.viewer.models.trace.Trace;
import codes.nibby.callsign.viewer.models.trace.TraceTrack;
import codes.nibby.callsign.viewer.ui.CanvasContainer;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import javax.annotation.Nullable;
import java.util.Map;

final class TraceViewGutter {

    private final CanvasContainer canvasContainer;
    private final GutterCanvas canvas;

    public TraceViewGutter() {
        canvas = new GutterCanvas();
        canvasContainer = new CanvasContainer(canvas);
    }

    public Node getComponent() {
        return canvasContainer;
    }

    public void paint(TraceViewViewport viewport, TraceCollection traces, TraceViewColorScheme colorScheme) {
        canvas.paint(viewport, traces, colorScheme);
    }

    private static final class GutterCanvas extends Canvas {

        private final GraphicsContext graphics;

        public GutterCanvas() {
            graphics = getGraphicsContext2D();
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

            graphics.setFill(Color.BLACK);
            graphics.fillText(track.getBinningAttributeName() + ": " + track.getBinningAttributeValue(), 10, yStart + trackHeight / 2);
        }
    }
}
