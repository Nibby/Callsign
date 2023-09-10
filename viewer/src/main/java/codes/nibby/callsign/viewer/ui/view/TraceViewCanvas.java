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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        }
    }

    private void paintBackground(TraceViewColorScheme colorScheme) {
        graphics.setFill(colorScheme.getContentBackground());
        graphics.fillRect(0, 0, getWidth(), getHeight());
    }

    private void paintContent(TraceViewPerspective perspective, TraceContent traces, TraceViewDisplayOptions displayOptions) {
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

        graphics.setFill(rowIndex % 2 == 0 ? colorScheme.getContentRowBackground() : colorScheme.getContentAlternateRowBackground());
        graphics.fillRect(perspective.getViewportStartX(), yStart, perspective.getViewportWidth(), bandHeight);

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

        String binningInfo = track.getBinningAttributeName() + ": " + track.getBinningAttributeValue();

        var binningInfoText = new Text(binningInfo);
        binningInfoText.setFont(font);

        graphics.fillText(binningInfo, perspective.getGutterWidth() - 5 - binningInfoText.getBoundsInLocal().getWidth(), yStart + 20);

        yStart += totalTrackHeight;

        return yStart;
    }
}
