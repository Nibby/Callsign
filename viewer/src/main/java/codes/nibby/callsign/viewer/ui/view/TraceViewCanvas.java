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
import java.util.concurrent.TimeUnit;

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

        paintTimelineIndicatorLinesInContent(perspective, displayOptions);
        paintTraces(perspective, traces, displayOptions);
    }

    private void paintTimelineIndicatorLinesInContent(TraceViewPerspective perspective, TraceViewDisplayOptions displayOptions) {
        TraceViewColorScheme colorScheme = displayOptions.getColorScheme();
        graphics.setFill(colorScheme.getTimelineIndicatorLinesInContentForeground());

        var x = perspective.getViewportStartX();
        var y = perspective.getViewportStartY();
        var height = perspective.getViewportHeight();

        TraceViewPerspective.MajorTickIncrementDescriptor majorTickDescriptor = perspective.getTimelineMajorTickDescriptor();
        long startTime = perspective.getTimeNsFromDisplayX(0);
        long startTimeExcess = startTime % majorTickDescriptor.timeNs();
        if (startTimeExcess != 0) {
            startTime -= startTimeExcess;
        }

        long endTime = perspective.getTimeNsFromDisplayX(getWidth() / 6 * 7);
        long endTimeExcess = endTime % majorTickDescriptor.timeNs();
        if (endTimeExcess != 0) {
            endTime += (majorTickDescriptor.timeNs() - endTimeExcess);
        }

        for (long timeNs = startTime; timeNs < endTime; timeNs += majorTickDescriptor.timeNs()) {
            double majorTickX = perspective.getDisplayX(timeNs);
            graphics.fillRect(x + majorTickX, y, 1, height);
        }
    }

    private void paintTraces(TraceViewPerspective perspective, TraceContent traces, TraceViewDisplayOptions displayOptions) {
        TraceContent.DisplayData displayData = traces.getDisplayData();
        Map<TraceTrack, TrackData> trackDatum = traces.getTrackData();
        int rowIndex = 0;
        double yStart = perspective.getViewportStartY() - perspective.getDisplayOffsetY();

        int startBandIndex = Math.max(0, (int) Math.floor(perspective.getDisplayOffsetY() / perspective.getTrackBandHeight()) - 2);

        int endBandIndex = Math.min(
            displayData.getTotalBands(),
            startBandIndex + (int) Math.ceil((perspective.getViewportHeight() + perspective.getTimelineIndicatorHeight()) / perspective.getTrackBandHeight()) + 2
        );

        yStart += startBandIndex * perspective.getTrackBandHeight();

        for (int cumulativeBandIndex = startBandIndex; cumulativeBandIndex < endBandIndex; cumulativeBandIndex++) {
            Optional<TraceContent.DisplayData.TrackDisplayData> trackDisplayData = displayData.getTrackDataFromCumulativeBandIndex(cumulativeBandIndex);
            if (trackDisplayData.isEmpty()) {
                continue;
            }

            TraceTrack track = trackDisplayData.get().track();
            @Nullable TrackData trackData = trackDatum.get(track);

            if (trackData == null) {
                continue;
            }

            List<Trace> bandTraces = trackData.getTraces(cumulativeBandIndex - trackDisplayData.get().cumulativeBandDisplayIndexStart());
            paintTraceBand(perspective, rowIndex, track, bandTraces, displayOptions, yStart);

            yStart += perspective.getTrackBandHeight();
        }
    }

    private void paintTraceBand(
        TraceViewPerspective perspective,
        int rowIndex,
        TraceTrack track,
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
                double xStart = perspective.getDisplayX(intervalTrace.getStartTimeNs());
                double width = perspective.getDisplayWidth((intervalTrace.getEndTimeNs() - intervalTrace.getStartTimeNs()));

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
                double xStart = perspective.getDisplayX(instantTrace.getTimeNs());
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
        var width = getWidth() - perspective.getViewportStartX();
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
        long startTime = perspective.getTimeNsFromDisplayX(0);
        long startTimeExcess = startTime % majorTickDescriptor.timeNs();
        if (startTimeExcess != 0) {
            startTime -= startTimeExcess;
        }

        long endTime = perspective.getTimeNsFromDisplayX(getWidth() / 6 * 7);
        long endTimeExcess = endTime % majorTickDescriptor.timeNs();
        if (endTimeExcess != 0) {
            endTime += (majorTickDescriptor.timeNs() - endTimeExcess);
        }

        for (long timeNs = startTime; timeNs < endTime; timeNs += majorTickDescriptor.timeNs()) {
            double majorTickX = perspective.getDisplayX(timeNs);
            graphics.fillRect(x + majorTickX, height - borderHeight - majorTickHeight, 1, majorTickHeight);

            String dateText = null, timeText = null;
            long timeInMilliseconds = TimeUnit.NANOSECONDS.toMillis(timeNs);
            Date dateAndTime = new Date(timeInMilliseconds);

            if (majorTickDescriptor.dateFormat() != null) {
                dateText = majorTickDescriptor.dateFormat().format(dateAndTime);
            }

            if (majorTickDescriptor.timeFormat() != null) {
                timeText = majorTickDescriptor.timeFormat().format(dateAndTime);
            }

            if (dateText == null && timeText == null) {
                timeText = String.valueOf(TimeUnit.NANOSECONDS.toMillis(timeNs));
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
