package codes.nibby.callsign.viewer.ui.view;

import codes.nibby.callsign.viewer.models.trace.InstantTrace;
import codes.nibby.callsign.viewer.models.trace.IntervalTrace;
import codes.nibby.callsign.viewer.models.trace.Trace;
import codes.nibby.callsign.viewer.models.trace.TraceTrack;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

final class TraceViewCanvas extends Canvas {

    private final GraphicsContext graphics;

    public TraceViewCanvas() {
        this.graphics = getGraphicsContext2D();
    }

    public void paint(TraceViewViewport viewport, @Nullable TraceContent traces, TraceViewDisplayOptions displayOptions) {
        paintBackground(displayOptions.getColorScheme());

        if (traces != null) {
            paintTrackContent(viewport, traces, displayOptions);
            paintTrackHeader(viewport, traces, displayOptions);
            paintTimelineIndicatorHeaders(viewport, displayOptions);
        }
    }

    private void paintBackground(TraceViewColorScheme colorScheme) {
        graphics.setFill(colorScheme.getContentBackground());
        graphics.fillRect(0, 0, getWidth(), getHeight());
    }

    private void paintTrackContent(TraceViewViewport viewport, TraceContent traces, TraceViewDisplayOptions displayOptions) {
        graphics.setFill(displayOptions.getColorScheme().getContentRowBackground());

        Rectangle2D contentBounds = viewport.getTrackContentBounds();
        graphics.fillRect(contentBounds.getMinX(), contentBounds.getMinY(), contentBounds.getWidth(), contentBounds.getHeight());

        paintTraceBackground(viewport, traces, displayOptions);
        paintTimelineIndicatorLinesInContent(viewport, displayOptions);
        paintTraces(viewport, traces, displayOptions);
    }

    private void paintTraceBackground(TraceViewViewport viewport, TraceContent traces, TraceViewDisplayOptions displayOptions) {
        TraceViewColorScheme colorScheme = displayOptions.getColorScheme();

        Rectangle2D contentBounds = viewport.getTrackContentBounds();

        graphics.setFill(colorScheme.getContentBackground());
        graphics.fillRect(0, 0, getWidth(), getHeight());

        TraceContent.DisplayData displayData = traces.getDisplayData();

        double yStart = contentBounds.getMinY() - viewport.getTrackContentOffsetY();

        forEachVisibleBandIndex(viewport, displayData.getTotalBands(), cumulativeBandIndex ->
            displayData.getTrackDataFromCumulativeBandIndex(cumulativeBandIndex).ifPresent(trackDisplayData -> {
                double bandY = yStart + cumulativeBandIndex * viewport.getTrackBandHeight();
                int displayIndex = trackDisplayData.trackDisplayIndex();
                boolean isAlternateRow = displayIndex % 2 == 1;

                graphics.setFill(isAlternateRow ? colorScheme.getContentAlternateRowBackground() : colorScheme.getContentRowBackground());
                graphics.fillRect(contentBounds.getMinX(), bandY, contentBounds.getWidth(), viewport.getTrackBandHeight());
            })
        );
    }

    private void forEachVisibleBandIndex(TraceViewViewport viewport, int totalBands, Consumer<Integer> cumulativeBandIndexConsumer) {
        Rectangle2D contentBounds = viewport.getTrackContentBounds();
        Rectangle2D timelineBounds = viewport.getTimelineBounds();

        int startBandIndex = Math.max(0, (int) Math.floor(viewport.getTrackContentOffsetY() / viewport.getTrackBandHeight()) - 2);

        int endBandIndex = Math.min(
            totalBands,
            startBandIndex + (int) Math.ceil((contentBounds.getHeight() + timelineBounds.getHeight()) / viewport.getTrackBandHeight()) + 2
        );

        for (int cumulativeBandIndex = startBandIndex; cumulativeBandIndex < endBandIndex; cumulativeBandIndex++) {
            cumulativeBandIndexConsumer.accept(cumulativeBandIndex);
        }
    }

    private void paintTimelineIndicatorLinesInContent(TraceViewViewport viewport, TraceViewDisplayOptions displayOptions) {
        TraceViewColorScheme colorScheme = displayOptions.getColorScheme();
        graphics.setFill(colorScheme.getTimelineDescriptorTickInContentForeground());

        Rectangle2D contentBounds = viewport.getTrackContentBounds();

        forEachTimelineDescriptor(viewport, descriptorTimeMs ->
            graphics.fillRect(
                contentBounds.getMinX() + viewport.translateToTrackContentX(descriptorTimeMs),
                contentBounds.getMinY(),
                1,
                contentBounds.getHeight()
            )
        );
    }

    private void forEachTimelineDescriptor(TraceViewViewport viewport, Consumer<Long> descriptorTimeMsConsumer) {
        TraceViewViewport.TimelineDescriptor majorTickDescriptor = viewport.getTimelineDescriptor();
        long startTime = viewport.translateToTimeMs(0);
        long startTimeExcess = startTime % majorTickDescriptor.getIncrementTimeMs();
        if (startTimeExcess != 0) {
            startTime -= startTimeExcess;
        }

        long endTime = viewport.translateToTimeMs(getWidth() / 6 * 7);
        long endTimeExcess = endTime % majorTickDescriptor.getIncrementTimeMs();
        if (endTimeExcess != 0) {
            endTime += (majorTickDescriptor.getIncrementTimeMs() - endTimeExcess);
        }

        for (long timeMs = startTime; timeMs < endTime; timeMs += majorTickDescriptor.getIncrementTimeMs()) {
            descriptorTimeMsConsumer.accept(timeMs);
        }
    }

    private void paintTraces(TraceViewViewport viewport, TraceContent traces, TraceViewDisplayOptions displayOptions) {
        TraceContent.DisplayData displayData = traces.getDisplayData();
        Map<TraceTrack, TrackData> trackDatum = traces.getTrackData();

        Rectangle2D contentBounds = viewport.getTrackContentBounds();

        double yStart = contentBounds.getMinY() - viewport.getTrackContentOffsetY();

        forEachVisibleBandIndex(viewport, displayData.getTotalBands(), cumulativeBandIndex ->
            displayData.getTrackDataFromCumulativeBandIndex(cumulativeBandIndex).ifPresent(trackDisplayData -> {
                TraceTrack track = trackDisplayData.track();
                @Nullable TrackData trackData = trackDatum.get(track);

                if (trackData == null) {
                    return;
                }

                double y = yStart + cumulativeBandIndex * viewport.getTrackBandHeight();

                Set<Trace> bandTraces = trackData.getTraces(cumulativeBandIndex - trackDisplayData.cumulativeBandDisplayIndexStart());
                paintTraceBand(viewport, bandTraces, displayOptions, y);
            })
        );
    }

    private void paintTraceBand(
        TraceViewViewport viewport,
        Collection<Trace> bandTraces,
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
                double xStart = viewport.translateToTrackContentX(intervalTrace.getStartTimeMs());
                double width = viewport.measureDisplayedWidth((intervalTrace.getEndTimeMs() - intervalTrace.getStartTimeMs()));

                graphics.setFill(colorScheme.getIntervalTraceEventBackground());
                graphics.fillRect(xStart, yStart + 4, width, bandHeight - 8);

                graphics.setStroke(colorScheme.getIntervalTraceEventOutline());
                graphics.strokeRect(xStart, yStart + 4, width, bandHeight - 8);

            } else if (displayOptions.isShowInstantTraces() && trace instanceof InstantTrace instantTrace) {
                instantTraces.add(instantTrace);
            }
        }

        if (displayOptions.isShowInstantTraces()) {
            for (InstantTrace instantTrace : instantTraces) {
                double xStart = viewport.translateToTrackContentX(instantTrace.getTimeMs());
                double size = bandHeight - 18;

                graphics.setFill(colorScheme.getInstantTraceEventBackground());
                graphics.fillOval(xStart - size / 2, yStart + bandHeight / 2 - size / 2, size, size);

                graphics.setStroke(colorScheme.getInstantTraceEventOutline());
                graphics.strokeOval(xStart - size / 2, yStart + bandHeight / 2 - size / 2, size, size);
            }
        }
    }

    private void paintTrackHeader(TraceViewViewport viewport, TraceContent traces, TraceViewDisplayOptions displayOptions) {
        TraceViewColorScheme colorScheme = displayOptions.getColorScheme();
        
        Rectangle2D trackHeaderBounds = viewport.getTrackHeaderBounds();
        Rectangle2D trackContentBounds = viewport.getTrackContentBounds();

        graphics.setFill(colorScheme.getGutterBackground());
        graphics.fillRect(0, 0, trackHeaderBounds.getWidth(), getHeight());

        Map<TraceTrack, TrackData> trackDatum = traces.getTrackData();
        int trackIndex = 0;
        double yStart = trackContentBounds.getMinY() - viewport.getTrackContentOffsetY();

        for (TraceTrack track : trackDatum.keySet()) {
            @Nullable TrackData trackData = trackDatum.get(track);

            if (trackData == null) {
                continue;
            }

            yStart = paintTraceTrackHeader(viewport, trackIndex, track, trackData.getBandCount(), colorScheme, yStart);

            trackIndex++;
        }
    }

    private void paintTimelineIndicatorHeaders(TraceViewViewport viewport, TraceViewDisplayOptions displayOptions) {
        TraceViewColorScheme colorScheme = displayOptions.getColorScheme();

        Rectangle2D contentBounds = viewport.getTrackContentBounds();
        Rectangle2D timelineBounds = viewport.getTimelineBounds();

        var x = contentBounds.getMinX();
        var height = timelineBounds.getHeight();

        final int borderHeight = 1;
        final int majorTickHeight = 5;

        // Background
        graphics.setFill(colorScheme.getTimelineBackground());
        graphics.fillRect(0, 0, getWidth(), height);

        graphics.setFill(colorScheme.getTimelineBorderBackground());
        graphics.fillRect(0, height - borderHeight, getWidth(), borderHeight);

        // Major ticks
        graphics.setFill(colorScheme.getTimelineDescriptorTick());

        var timelineDescriptor = viewport.getTimelineDescriptor();

        forEachTimelineDescriptor(viewport, timeMs -> {
            double majorTickX = viewport.translateToTrackContentX(timeMs);
            graphics.fillRect(x + majorTickX, height - borderHeight - majorTickHeight, 1, majorTickHeight);

            String dateText = null, timeText = null;
            Date dateAndTime = new Date(timeMs);

            if (timelineDescriptor.dateFormat() != null) {
                dateText = timelineDescriptor.dateFormat().format(dateAndTime);
            }

            if (timelineDescriptor.timeFormat() != null) {
                timeText = timelineDescriptor.timeFormat().format(dateAndTime);
            }

            if (dateText == null && timeText == null) {
                timeText = String.valueOf(timeMs);
            }

            var textY = height - borderHeight;

            graphics.setFill(colorScheme.getTimelineText());

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
        });
    }

    private double paintTraceTrackHeader(
        TraceViewViewport viewport,
        int trackIndex,
        TraceTrack track,
        int bandCount,
        TraceViewColorScheme colorScheme,
        double yStart
    ) {
        double totalTrackHeight = viewport.getTrackBandHeight() * bandCount;
        Rectangle2D trackHeaderBounds = viewport.getTrackHeaderBounds();

        graphics.setFill(trackIndex % 2 == 0 ? colorScheme.getGutterRowBackground() : colorScheme.getGutterAlternateRowBackground());
        graphics.fillRect(0, yStart, trackHeaderBounds.getWidth(), totalTrackHeight);

        graphics.setFill(Color.WHITE);
        graphics.setFontSmoothingType(FontSmoothingType.GRAY);

        var font = Font.font(Font.getDefault().getFamily(), FontWeight.BLACK, 14d);
        graphics.setFont(font);

        String binningInfo = track.getBinningAttributeValue();

        var binningInfoText = new Text(binningInfo);
        binningInfoText.setFont(font);

        graphics.fillText(binningInfo, trackHeaderBounds.getWidth() - 5 - binningInfoText.getBoundsInLocal().getWidth(), yStart + 20);

        yStart += totalTrackHeight;

        return yStart;
    }
}
