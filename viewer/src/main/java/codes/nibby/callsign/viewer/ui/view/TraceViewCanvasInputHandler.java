package codes.nibby.callsign.viewer.ui.view;

import codes.nibby.callsign.viewer.models.trace.InstantTrace;
import codes.nibby.callsign.viewer.models.trace.IntervalTrace;
import codes.nibby.callsign.viewer.models.trace.Trace;
import javafx.scene.input.MouseEvent;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;

final class TraceViewCanvasInputHandler {

    public void handleMouseMove(
        MouseEvent event,
        TraceViewViewport viewport,
        TraceViewSelectionManager selection,
        @Nullable TraceContent traces
    ) {
        Collection<Trace> intersection = getTracesIntersectingMouseCursor(event, viewport, traces);
        selection.setHoveredTraces(intersection);
    }

    public void handleMousePress(
        MouseEvent event,
        TraceViewViewport viewport,
        TraceViewSelectionManager selection,
        @Nullable TraceContent traces
    ) {
        Collection<Trace> intersection = getTracesIntersectingMouseCursor(event, viewport, traces);

        if (!event.isControlDown()) {
            selection.setSelectedTraces(intersection);
        } else if (!intersection.isEmpty()) {
            Trace firstSelection = intersection.iterator().next();
            selection.toggleSelectedTrace(firstSelection);
        }
    }

    private Collection<Trace> getTracesIntersectingMouseCursor(MouseEvent event, TraceViewViewport viewport, TraceContent traces) {
        if (traces == null) {
            return Collections.emptySet();
        }

        double viewportY = event.getY();
        double mouseXInContent = Math.max(0, event.getX());

        int hoveredCumulativeBandIndex = viewport.translateToCumulativeBandIndex(viewportY);

        if (hoveredCumulativeBandIndex < 0 || hoveredCumulativeBandIndex >= traces.getTotalDisplayableBands()) {
            return Collections.emptySet();
        }

        Optional<TraceContent.TrackDisplayData> trackDisplayDataValue = traces.getTrackDisplayData(hoveredCumulativeBandIndex);

        if (trackDisplayDataValue.isEmpty()) {
            return Collections.emptySet();
        }

        TraceContent.TrackDisplayData trackDisplayData = trackDisplayDataValue.get();
        Collection<Trace> bandTraces = trackDisplayData.getTracesFromCumulativeBandIndex(hoveredCumulativeBandIndex);

        Collection<Trace> intersection = new LinkedHashSet<>();

        for (Trace trace : bandTraces) {
            boolean intersecting;

            if (trace instanceof IntervalTrace intervalTrace) {
                long startTimeMs = intervalTrace.getStartTimeMs();
                long endTimeMs = intervalTrace.getEndTimeMs();

                intersecting = testMouseIntersection(startTimeMs, endTimeMs, mouseXInContent, viewport);
            } else if (trace instanceof InstantTrace instantTrace) {
                double size = viewport.getInstantTraceSize();
                long startTimeMs = instantTrace.getTimeMs() - viewport.translateToTimeMs(size / 2);
                long endTimeMs = instantTrace.getTimeMs() + viewport.translateToTimeMs(size / 2);

                intersecting = testMouseIntersection(startTimeMs, endTimeMs, mouseXInContent, viewport);
            } else {
                intersecting = false;
            }

            if (intersecting) {
                intersection.add(trace);
            }
        }

        return intersection;
    }

    private boolean testMouseIntersection(
        long timeMsStart,
        long timeMsEnd,
        double mouseXInContent,
        TraceViewViewport viewport
    ) {
        if (!viewport.isTimeMsVisible(timeMsStart) && !viewport.isTimeMsVisible(timeMsEnd)) {
            return false;
        }

        double traceStartX = viewport.translateToTrackContentX(timeMsStart);
        double traceEndX = viewport.translateToTrackContentX(timeMsEnd);

        return mouseXInContent >= traceStartX && mouseXInContent <= traceEndX;
    }

    public void handleMouseExit(
        MouseEvent event,
        TraceViewViewport viewport,
        TraceViewSelectionManager selection
    ) {

    }
}
