package codes.nibby.callsign.viewer.ui.view;

import codes.nibby.callsign.api.Event;
import codes.nibby.callsign.viewer.models.TraceDocument;
import codes.nibby.callsign.viewer.ui.CanvasContainer;
import javafx.scene.layout.BorderPane;

public final class TraceViewContent {

    private final BorderPane contentPane;

    private TraceDocument document;

    private TraceViewColorScheme colorScheme;
    private final TraceViewContentManager contentManager;
    private final TraceViewViewport viewport;

    private final TraceViewCanvas canvas;


    public TraceViewContent() {
        contentPane = new BorderPane();

        canvas = new TraceViewCanvas();
        colorScheme = new TraceViewLightColorScheme();
        contentManager = new TraceViewContentManager();
        viewport = new TraceViewViewport();

        var canvasWrapper = new CanvasContainer(canvas);
        canvasWrapper.addSizeUpdateListener(newSize -> refreshContent(newSize.getWidth(), newSize.getHeight()));

        contentPane.setCenter(canvasWrapper);
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

        viewport.recompute(width, height, earliestEventTimeNs, latestEventTimeNs);

        TraceCollection viewableEvents = contentManager.compute(Event.SPECIAL_NAME_ATTRIBUTE, document);
        canvas.paint(viewport, viewableEvents, colorScheme);
    }

    public BorderPane getComponent() {
        return contentPane;
    }

    public void setDocument(TraceDocument document) {
        this.document = document;
        refreshContent();
    }
}
