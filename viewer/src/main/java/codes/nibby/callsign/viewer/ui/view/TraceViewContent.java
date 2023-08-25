package codes.nibby.callsign.viewer.ui.view;

import codes.nibby.callsign.viewer.models.TraceDocument;
import codes.nibby.callsign.viewer.ui.CanvasContainer;
import javafx.scene.layout.BorderPane;

public final class TraceViewContent {

    private final BorderPane contentPane;

    private TraceDocument document;

    private TraceViewColorScheme colorScheme;
    private final TraceViewContentManager contentLayoutManager;

    private final TraceViewCanvas canvas;


    public TraceViewContent() {
        contentPane = new BorderPane();

        canvas = new TraceViewCanvas();

        colorScheme = new TraceViewLightColorScheme();
        contentLayoutManager = new TraceViewContentManager();

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

        canvas.paint(document);
    }

    public BorderPane getComponent() {
        return contentPane;
    }

    public void setDocument(TraceDocument document) {
        this.document = document;
        refreshContent();
    }
}
