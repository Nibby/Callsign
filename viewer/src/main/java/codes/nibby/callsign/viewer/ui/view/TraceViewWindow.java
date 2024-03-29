package codes.nibby.callsign.viewer.ui.view;

import codes.nibby.callsign.viewer.models.document.TraceDocument;
import codes.nibby.callsign.viewer.models.document.TraceDocumentAccessException;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class TraceViewWindow {

    private final Stage stage;
    private final Scene scene;
    private final TraceViewContentPane content;

    public TraceViewWindow() {
        content = new TraceViewContentPane();

        scene = new Scene(content.getComponent(), 800, 600);

        stage = new Stage();
        stage.setScene(scene);
    }

    public void setOnClose(Runnable runnable) {
        stage.setOnCloseRequest(event -> runnable.run());
    }

    public void initialize(TraceDocument document) {

        try {
            document.load();
        } catch (TraceDocumentAccessException e) {
            throw new RuntimeException(e);
        }

        content.setDocument(document);
    }

    public void show() {
        stage.show();
    }
}
