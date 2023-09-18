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

    public void paint(TraceViewPerspective perspective, @Nullable TraceContent traces, TraceViewDisplayOptions displayOptions) {
        paintBackground(displayOptions.getColorScheme());

        if (traces != null) {
            paintContent(perspective, traces, displayOptions);
            paintGutter(perspective, traces, displayOptions);
            paintTimelineIndicatorHeaders(perspective, displayOptions);
        }
    }

    private void paintBackground(TraceViewColorScheme colorScheme) {
        graphics.setFill(colorScheme.getContentBackground());
        graphics.fillRect(0, 0, getWidth(), getHeight());
    }

    private void paintContent(TraceViewPerspective perspective, TraceContent traces, TraceViewDisplayOptions displayOptions) {
        graphics.setFill(displayOptions.getColorScheme().getContentRowBackground());
        graphics.fillRect(perspective.getViewportStartX(), perspective.getViewportStartY(), perspective.getViewportWidth(), perspective.getViewportHeight());

        paintTraceBackground(perspective, traces, displayOptions);
        paintTimelineIndicatorLinesInContent(perspective, displayOptions);
        paintTraces(perspective, traces, displayOptions);
    }

    private void paintTraceBackground(TraceViewPerspective perspective, TraceContent traces, TraceViewDisplayOptions displayOptions) {
        TraceViewColorScheme colorScheme = displayOptions.getColorScheme();
        graphics.setFill(colorScheme.getContentBackground());
        graphics.fillRect(0, 0, getWidth(), getHeight());

        TraceContent.DisplayData displayData = traces.getDisplayData();
        double yStart = perspective.getViewportStartY() - perspective.getDisplayOffsetY();

        int startBandIndex = Math.max(0, (int) Math.floor(perspective.getDisplayOffsetY() / perspective.getTrackBandHeight()) - 2);

        int endBandIndex = Math.min(
            displayData.getTotalBands(),
            startBandIndex + (int) Math.ceil((perspective.getViewportHeight() + perspective.getTimelineIndicatorHeight()) / perspective.getTrackBandHeight()) + 2
        );

        yStart += startBandIndex * perspective.getTrackBandHeight();

        for (int cumulativeBandIndex = startBandIndex; cumulativeBandIndex < endBandIndex; cumulativeBandIndex++) {
            Optional<TraceContent.DisplayData.TrackDisplayData> trackDisplayDataOptional = displayData.getTrackDataFromCumulativeBandIndex(cumulativeBandIndex);

            if (trackDisplayDataOptional.isEmpty()) {
                continue;
            }

            var trackDisplayData = trackDisplayDataOptional.get();
            int displayIndex = trackDisplayData.trackDisplayIndex();

            boolean isAlternateRow = displayIndex % 2 == 1;

            graphics.setFill(isAlternateRow ? colorScheme.getContentAlternateRowBackground() : colorScheme.getContentRowBackground());
            graphics.fillRect(perspective.getViewportStartX(), yStart, perspective.getViewportWidth(), perspective.getViewportHeight());

            yStart += perspective.getTrackBandHeight();
        }
    }

    private void paintTimelineIndicatorLinesInContent(TraceViewPerspective perspective, TraceViewDisplayOptions displayOptions) {
        TraceViewColorScheme colorScheme = displayOptions.getColorScheme();
        graphics.setFill(colorScheme.getTimelineIndicatorLinesInContentForeground());

        var x = perspective.getViewportStartX();
        var y = perspective.getViewportStartY();
        var height = perspective.getViewportHeight();

        TraceViewPerspective.MajorTickIncrementDescriptor majorTickDescriptor = perspective.getTimelineMajorTickDescriptor();
        long startTime = perspective.getTimeMsFromDisplayX(0);
        long startTimeExcess = startTime % majorTickDescriptor.timeMs();
        if (startTimeExcess != 0) {
            startTime -= startTimeExcess;
        }

        long endTime = perspective.getTimeMsFromDisplayX(getWidth() / 6 * 7);
        long endTimeExcess = endTime % majorTickDescriptor.timeMs();
        if (endTimeExcess != 0) {
            endTime += (majorTickDescriptor.timeMs() - endTimeExcess);
        }

        for (long timeMs = startTime; timeMs < endTime; timeMs += majorTickDescriptor.timeMs()) {
            double majorTickX = perspective.getDisplayX(timeMs);
            graphics.fillRect(x + majorTickX, y, 1, height);
        }
    }

    private void paintTraces(TraceViewPerspective perspective, TraceContent traces, TraceViewDisplayOptions displayOptions) {
        TraceContent.DisplayData displayData = traces.getDisplayData();
        Map<TraceTrack, TrackData> trackDatum = traces.getTrackData();
        double yStart = perspective.getViewportStartY() - perspective.getDisplayOffsetY();

        int startBandIndex = Math.max(0, (int) Math.floor(perspective.getDisplayOffsetY() / perspective.getTrackBandHeight()) - 2);

        int endBandIndex = Math.min(
            displayData.getTotalBands(),
            startBandIndex + (int) Math.ceil((perspective.getViewportHeight() + perspective.getTimelineIndicatorHeight()) / perspective.getTrackBandHeight()) + 2
        );

        yStart += startBandIndex * perspective.getTrackBandHeight();

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
            paintTraceBand(perspective, bandTraces, displayOptions, yStart);

            yStart += perspective.getTrackBandHeight();
        }
    }

    private void paintTraceBand(
        TraceViewPerspective perspective,
        List<Trace> bandTraces,
        TraceViewDisplayOptions displayOptions,
        double yStart
    ) {
        TraceViewColorScheme colorScheme = displayOptions.getColorScheme();
        double bandHeight = perspective.getTrackBandHeight();

        List<InstantTrace> instantTraces = new ArrayList<>();

        if (!displayOptions.isShowInstantTraces() && !displayOptions.isShowIntervalTraces()) {
            return;
        }

        for (Trace trace : bandTraces) {
            if (displayOptions.isShowIntervalTraces() && trace instanceof IntervalTrace intervalTrace) {
                double xStart = perspective.getDisplayX(intervalTrace.getStartTimeMs());
                double width = perspective.getDisplayWidth((intervalTrace.getEndTimeMs() - intervalTrace.getStartTimeMs()));

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
                double xStart = perspective.getDisplayX(instantTrace.getTimeMs());
                double size = bandHeight - 18;

                graphics.setFill(colorScheme.getInstantTraceEventBackground());
                graphics.fillOval(xStart - size / 2, yStart + bandHeight / 2 - size / 2, size, size);

                graphics.setStroke(colorScheme.getInstantTraceEventOutline());
                graphics.strokeOval(xStart - size / 2, yStart + bandHeight / 2 - size / 2, size, size);
            }
        }
    }

    private void paintGutter(TraceViewPerspective perspective, TraceContent traces, TraceViewDisplayOptions displayOptions) {
        TraceViewColorScheme colorScheme = displayOptions.getColorScheme();

        graphics.setFill(colorScheme.getGutterBackground());
        graphics.fillRect(0, 0, perspective.getGutterWidth(), getHeight());

        Map<TraceTrack, TrackData> trackDatum = traces.getTrackData();
        int trackIndex = 0;
        double yStart = perspective.getViewportStartY() - perspective.getDisplayOffsetY();

        for (TraceTrack track : trackDatum.keySet()) {
            @Nullable TrackData trackData = trackDatum.get(track);

            if (trackData == null) {
                continue;
            }

            yStart = paintTraceTrackGutter(perspective, trackIndex, track, trackData.getBandCount(), colorScheme, yStart);

            trackIndex++;
        }
    }

    private void paintTimelineIndicatorHeaders(TraceViewPerspective perspective, TraceViewDisplayOptions displayOptions) {
        TraceViewColorScheme colorScheme = displayOptions.getColorScheme();

        var x = perspective.getViewportStartX();
        var height = perspective.getTimelineIndicatorHeight();

        final int borderHeight = 1;
        final int majorTickHeight = 5;

        // Background
        graphics.setFill(colorScheme.getTimelineBackground());
        graphics.fillRect(0, 0, getWidth(), height);
        graphics.setFill(colorScheme.getTimelineBorderBackground());
        graphics.fillRect(0, height - borderHeight, getWidth(), borderHeight);

        // Major ticks
        graphics.setFill(colorScheme.getTimelineMajorTick());

        TraceViewPerspective.MajorTickIncrementDescriptor majorTickDescriptor = perspective.getTimelineMajorTickDescriptor();
        long startTime = perspective.getTimeMsFromDisplayX(0);
        long startTimeExcess = startTime % majorTickDescriptor.timeMs();
        if (startTimeExcess != 0) {
            startTime -= startTimeExcess;
        }

        long endTime = perspective.getTimeMsFromDisplayX(getWidth() / 6 * 7);
        long endTimeExcess = endTime % majorTickDescriptor.timeMs();
        if (endTimeExcess != 0) {
            endTime += (majorTickDescriptor.timeMs() - endTimeExcess);
        }

        for (long timeMs = startTime; timeMs < endTime; timeMs += majorTickDescriptor.timeMs()) {
            double majorTickX = perspective.getDisplayX(timeMs);
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
        TraceViewPerspective perspective,
        int trackIndex,
        TraceTrack track,
        int bandCount,
        TraceViewColorScheme colorScheme,
        double yStart
    ) {
        double totalTrackHeight = perspective.getTrackBandHeight() * bandCount;

        graphics.setFill(trackIndex % 2 == 0 ? colorScheme.getGutterRowBackground() : colorScheme.getGutterAlternateRowBackground());
        graphics.fillRect(0, yStart, perspective.getGutterWidth(), totalTrackHeight);

        graphics.setFill(Color.WHITE);
        graphics.setFontSmoothingType(FontSmoothingType.GRAY);

        var font = Font.font(Font.getDefault().getFamily(), FontWeight.BLACK, 14d);
        graphics.setFont(font);

        String binningInfo = track.getBinningAttributeValue();

        var binningInfoText = new Text(binningInfo);
        binningInfoText.setFont(font);

        graphics.fillText(binningInfo, perspective.getGutterWidth() - 5 - binningInfoText.getBoundsInLocal().getWidth(), yStart + 20);

        yStart += totalTrackHeight;

        return yStart;
    }
}
