package codes.nibby.callsign.viewer.ui.view;

import codes.nibby.callsign.viewer.models.trace.InstantTrace;
import codes.nibby.callsign.viewer.models.trace.IntervalTrace;
import codes.nibby.callsign.viewer.models.trace.Trace;
import codes.nibby.callsign.viewer.models.trace.TraceTrack;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import javax.annotation.Nullable;
import java.util.*;

final class TraceViewCanvas extends Canvas {

    private final GraphicsContext graphics;

    public TraceViewCanvas() {
        this.graphics = getGraphicsContext2D();
    }

    public void paint(TraceViewViewport viewport, @Nullable TraceContent traces, TraceViewDisplayOptions displayOptions) {
        paintBackground(displayOptions.getColorScheme());

        if (traces != null) {
            paintContent(viewport, traces, displayOptions);
            paintGutter(viewport, traces, displayOptions);
            paintTimelineIndicatorHeaders(viewport, displayOptions);
        }
    }

    private void paintBackground(TraceViewColorScheme colorScheme) {
        graphics.setFill(colorScheme.getContentBackground());
        graphics.fillRect(0, 0, getWidth(), getHeight());
    }

    private void paintContent(TraceViewViewport viewport, TraceContent traces, TraceViewDisplayOptions displayOptions) {
        graphics.setFill(displayOptions.getColorScheme().getContentRowBackground());
        graphics.fillRect(viewport.getViewportStartX(), viewport.getViewportStartY(), viewport.getViewportWidth(), viewport.getViewportHeight());

        paintTraceBackground(viewport, traces, displayOptions);
        paintTimelineIndicatorLinesInContent(viewport, displayOptions);
        paintTraces(viewport, traces, displayOptions);
    }

    private void paintTraceBackground(TraceViewViewport viewport, TraceContent traces, TraceViewDisplayOptions displayOptions) {
        TraceViewColorScheme colorScheme = displayOptions.getColorScheme();
        graphics.setFill(colorScheme.getContentBackground());
        graphics.fillRect(0, 0, getWidth(), getHeight());

        TraceContent.DisplayData displayData = traces.getDisplayData();
        double yStart = viewport.getViewportStartY() - viewport.getDisplayOffsetY();

        int startBandIndex = Math.max(0, (int) Math.floor(viewport.getDisplayOffsetY() / viewport.getTrackBandHeight()) - 2);

        int endBandIndex = Math.min(
            displayData.getTotalBands(),
            startBandIndex + (int) Math.ceil((viewport.getViewportHeight() + viewport.getTimelineIndicatorHeight()) / viewport.getTrackBandHeight()) + 2
        );

        yStart += startBandIndex * viewport.getTrackBandHeight();

        for (int cumulativeBandIndex = startBandIndex; cumulativeBandIndex < endBandIndex; cumulativeBandIndex++) {
            Optional<TraceContent.DisplayData.TrackDisplayData> trackDisplayDataOptional = displayData.getTrackDataFromCumulativeBandIndex(cumulativeBandIndex);

            if (trackDisplayDataOptional.isEmpty()) {
                continue;
            }

            var trackDisplayData = trackDisplayDataOptional.get();
            int displayIndex = trackDisplayData.trackDisplayIndex();

            boolean isAlternateRow = displayIndex % 2 == 1;

            graphics.setFill(isAlternateRow ? colorScheme.getContentAlternateRowBackground() : colorScheme.getContentRowBackground());
            graphics.fillRect(viewport.getViewportStartX(), yStart, viewport.getViewportWidth(), viewport.getTrackBandHeight());

            yStart += viewport.getTrackBandHeight();
        }
    }

    private void paintTimelineIndicatorLinesInContent(TraceViewViewport viewport, TraceViewDisplayOptions displayOptions) {
        TraceViewColorScheme colorScheme = displayOptions.getColorScheme();
        graphics.setFill(colorScheme.getTimelineIndicatorLinesInContentForeground());

        var x = viewport.getViewportStartX();
        var y = viewport.getViewportStartY();
        var height = viewport.getViewportHeight();

        TraceViewViewport.MajorTickIncrementDescriptor majorTickDescriptor = viewport.getTimelineMajorTickDescriptor();
        long startTime = viewport.getTimeMsFromDisplayX(0);
        long startTimeExcess = startTime % majorTickDescriptor.timeMs();
        if (startTimeExcess != 0) {
            startTime -= startTimeExcess;
        }

        long endTime = viewport.getTimeMsFromDisplayX(getWidth() / 6 * 7);
        long endTimeExcess = endTime % majorTickDescriptor.timeMs();
        if (endTimeExcess != 0) {
            endTime += (majorTickDescriptor.timeMs() - endTimeExcess);
        }

        for (long timeMs = startTime; timeMs < endTime; timeMs += majorTickDescriptor.timeMs()) {
            double majorTickX = viewport.getDisplayX(timeMs);
            graphics.fillRect(x + majorTickX, y, 1, height);
        }
    }

    private void paintTraces(TraceViewViewport viewport, TraceContent traces, TraceViewDisplayOptions displayOptions) {
        TraceContent.DisplayData displayData = traces.getDisplayData();
        Map<TraceTrack, TrackData> trackDatum = traces.getTrackData();
        double yStart = viewport.getViewportStartY() - viewport.getDisplayOffsetY();

        int startBandIndex = Math.max(0, (int) Math.floor(viewport.getDisplayOffsetY() / viewport.getTrackBandHeight()) - 2);

        int endBandIndex = Math.min(
            displayData.getTotalBands(),
            startBandIndex + (int) Math.ceil((viewport.getViewportHeight() + viewport.getTimelineIndicatorHeight()) / viewport.getTrackBandHeight()) + 2
        );

        yStart += startBandIndex * viewport.getTrackBandHeight();

        for (int cumulativeBandIndex = startBandIndex; cumulativeBandIndex < endBandIndex; cumulativeBandIndex++) {
            Optional<TraceContent.DisplayData.TrackDisplayData> trackDisplayDataOptional = displayData.getTrackDataFromCumulativeBandIndex(cumulativeBandIndex);

            if (trackDisplayDataOptional.isEmpty()) {
                continue;
            }

            var trackDisplayData = trackDisplayDataOptional.get();

            TraceTrack track = trackDisplayData.track();
            @Nullable TrackData trackData = trackDatum.get(track);

            if (trackData == null) {
                continue;
            }

            List<Trace> bandTraces = trackData.getTraces(cumulativeBandIndex - trackDisplayData.cumulativeBandDisplayIndexStart());
            paintTraceBand(viewport, bandTraces, displayOptions, yStart);

            yStart += viewport.getTrackBandHeight();
        }
    }

    private void paintTraceBand(
        TraceViewViewport viewport,
        List<Trace> bandTraces,
        TraceViewDisplayOptions displayOptions,
        double yStart
    ) {
        TraceViewColorScheme colorScheme = displayOptions.getColorScheme();
        double bandHeight = viewport.getTrackBandHeight();

        List<InstantTrace> instantTraces = new ArrayList<>();

        if (!displayOptions.isShowInstantTraces() && !displayOptions.isShowIntervalTraces()) {
            return;
        }

        for (Trace trace : bandTraces) {
            if (displayOptions.isShowIntervalTraces() && trace instanceof IntervalTrace intervalTrace) {
                double xStart = viewport.getDisplayX(intervalTrace.getStartTimeMs());
                double width = viewport.getDisplayWidth((intervalTrace.getEndTimeMs() - intervalTrace.getStartTimeMs()));

                graphics.setFill(colorScheme.getIntervalTraceEventBackground());
                graphics.fillRect(xStart, yStart + 6, width, bandHeight - 12);

                graphics.setStroke(colorScheme.getIntervalTraceEventOutline());
                graphics.strokeRect(xStart, yStart + 6, width, bandHeight - 12);

            } else if (displayOptions.isShowInstantTraces() && trace instanceof InstantTrace instantTrace) {
                instantTraces.add(instantTrace);
            }
        }

        if (displayOptions.isShowInstantTraces()) {
            for (InstantTrace instantTrace : instantTraces) {
                double xStart = viewport.getDisplayX(instantTrace.getTimeMs());
                double size = bandHeight - 18;

                graphics.setFill(colorScheme.getInstantTraceEventBackground());
                graphics.fillOval(xStart - size / 2, yStart + bandHeight / 2 - size / 2, size, size);

                graphics.setStroke(colorScheme.getInstantTraceEventOutline());
                graphics.strokeOval(xStart - size / 2, yStart + bandHeight / 2 - size / 2, size, size);
            }
        }
    }

    private void paintGutter(TraceViewViewport viewport, TraceContent traces, TraceViewDisplayOptions displayOptions) {
        TraceViewColorScheme colorScheme = displayOptions.getColorScheme();

        graphics.setFill(colorScheme.getGutterBackground());
        graphics.fillRect(0, 0, viewport.getGutterWidth(), getHeight());

        Map<TraceTrack, TrackData> trackDatum = traces.getTrackData();
        int trackIndex = 0;
        double yStart = viewport.getViewportStartY() - viewport.getDisplayOffsetY();

        for (TraceTrack track : trackDatum.keySet()) {
            @Nullable TrackData trackData = trackDatum.get(track);

            if (trackData == null) {
                continue;
            }

            yStart = paintTraceTrackGutter(viewport, trackIndex, track, trackData.getBandCount(), colorScheme, yStart);

            trackIndex++;
        }
    }

    private void paintTimelineIndicatorHeaders(TraceViewViewport viewport, TraceViewDisplayOptions displayOptions) {
        TraceViewColorScheme colorScheme = displayOptions.getColorScheme();

        var x = viewport.getViewportStartX();
        var height = viewport.getTimelineIndicatorHeight();

        final int borderHeight = 1;
        final int majorTickHeight = 5;

        // Background
        graphics.setFill(colorScheme.getTimelineBackground());
        graphics.fillRect(0, 0, getWidth(), height);
        graphics.setFill(colorScheme.getTimelineBorderBackground());
        graphics.fillRect(0, height - borderHeight, getWidth(), borderHeight);

        // Major ticks
        graphics.setFill(colorScheme.getTimelineMajorTick());

        TraceViewViewport.MajorTickIncrementDescriptor majorTickDescriptor = viewport.getTimelineMajorTickDescriptor();
        long startTime = viewport.getTimeMsFromDisplayX(0);
        long startTimeExcess = startTime % majorTickDescriptor.timeMs();
        if (startTimeExcess != 0) {
            startTime -= startTimeExcess;
        }

        long endTime = viewport.getTimeMsFromDisplayX(getWidth() / 6 * 7);
        long endTimeExcess = endTime % majorTickDescriptor.timeMs();
        if (endTimeExcess != 0) {
            endTime += (majorTickDescriptor.timeMs() - endTimeExcess);
        }

        for (long timeMs = startTime; timeMs < endTime; timeMs += majorTickDescriptor.timeMs()) {
            double majorTickX = viewport.getDisplayX(timeMs);
            graphics.fillRect(x + majorTickX, height - borderHeight - majorTickHeight, 1, majorTickHeight);

            String dateText = null, timeText = null;
            Date dateAndTime = new Date(timeMs);

            if (majorTickDescriptor.dateFormat() != null) {
                dateText = majorTickDescriptor.dateFormat().format(dateAndTime);
            }

            if (majorTickDescriptor.timeFormat() != null) {
                timeText = majorTickDescriptor.timeFormat().format(dateAndTime);
            }

            if (dateText == null && timeText == null) {
                timeText = String.valueOf(timeMs);
            }

            var textY = height - borderHeight;

            if (timeText != null) {
                var textBounds = new Text(timeText).getBoundsInLocal();
                var textX = x + majorTickX - textBounds.getWidth() / 2;
                textY -= textBounds.getHeight() / 2;

                graphics.fillText(timeText, textX, textY);
            }

            if (dateText != null) {
                var textBounds = new Text(dateText).getBoundsInLocal();
                var textX = x + majorTickX - textBounds.getWidth() / 2;
                textY -= textBounds.getHeight();

                graphics.fillText(timeText, textX, textY);
            }
        }
    }

    private double paintTraceTrackGutter(
        TraceViewViewport viewport,
        int trackIndex,
        TraceTrack track,
        int bandCount,
        TraceViewColorScheme colorScheme,
        double yStart
    ) {
        double totalTrackHeight = viewport.getTrackBandHeight() * bandCount;

        graphics.setFill(trackIndex % 2 == 0 ? colorScheme.getGutterRowBackground() : colorScheme.getGutterAlternateRowBackground());
        graphics.fillRect(0, yStart, viewport.getGutterWidth(), totalTrackHeight);

        graphics.setFill(Color.WHITE);
        graphics.setFontSmoothingType(FontSmoothingType.GRAY);

        var font = Font.font(Font.getDefault().getFamily(), FontWeight.BLACK, 14d);
        graphics.setFont(font);

        String binningInfo = track.getBinningAttributeValue();

        var binningInfoText = new Text(binningInfo);
        binningInfoText.setFont(font);

        graphics.fillText(binningInfo, viewport.getGutterWidth() - 5 - binningInfoText.getBoundsInLocal().getWidth(), yStart + 20);

        yStart += totalTrackHeight;

        return yStart;
    }
}
