package codes.nibby.callsign.viewer.ui.view;

import codes.nibby.callsign.viewer.models.document.TraceDocument;
import codes.nibby.callsign.viewer.models.filters.TraceFilters;
import codes.nibby.callsign.viewer.ui.CanvasContainer;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;

import java.util.Map;

public final class TraceViewContentPane {

    private final BorderPane contentPane;

    private TraceDocument document;

    private final TraceViewViewport viewport;
    private final TraceViewTraceContentManager contentManager;
    private final TraceViewColorScheme colorScheme;
    private final TraceFilters traceFilters;

    private final TraceViewCanvas canvas;
    private final TraceViewGutter gutter;
    private final SplitPane canvasGutterSplitter;

    public TraceViewContentPane() {
        contentPane = new BorderPane();

        traceFilters = new TraceFilters();
        colorScheme = new TraceViewLightColorScheme();
        contentManager = new TraceViewTraceContentManager();
        viewport = new TraceViewViewport();

        canvas = new TraceViewCanvas();
        var canvasWrapper = new CanvasContainer(canvas);
        canvasWrapper.addSizeUpdateListener(newSize -> refreshContent(newSize.getWidth(), newSize.getHeight()));

        gutter = new TraceViewGutter();

        canvasGutterSplitter = new SplitPane(gutter.getComponent(), canvasWrapper);
        canvasGutterSplitter.setDividerPosition(0, 0.25d);
        contentPane.setCenter(canvasGutterSplitter);
    }

    private void refreshContent() {
        this.refreshContent(canvas.getWidth(), canvas.getHeight());
    }

    private void refreshContent(double width, double height) {

        // TODO: More sophisticated handling of viewport data, for now just show everything
        //       The final procedure probably looks something like:
        //       1. Recompute correct viewport bounds
        //       2. Based on the timeline in view port, craft filters for selecting trace entries in the document
        //       3. Stream each filtered trace entry from the document & paint it to an off-screen image
        //       4. Paint the off-screen image to canvas

        long earliestEventTimeNs = document.getEarliestEventStartTimeNs();
        long latestEventTimeNs = document.getLatestEventEndTimeNs();

        boolean viewportChanged = viewport.applyProperties(width, height, earliestEventTimeNs, latestEventTimeNs);

        long displayedEarliestTimeNs = viewport.getDisplayedEarliestEventTimeNs();
        long displayedLatestTimeNs = viewport.getDisplayedLatestEventTimeNs();

        traceFilters.setDisplayedTimeInterval(displayedEarliestTimeNs, displayedLatestTimeNs);

        TraceCollection traces = contentManager.computeContent(document, "index", traceFilters);

        gutter.paint(viewport, traces, colorScheme);
        canvas.paint(viewport, traces, colorScheme);
    }

    public BorderPane getComponent() {
        return contentPane;
    }

    public void setDocument(TraceDocument document) {
        this.document = document;
        refreshContent();
    }
}
