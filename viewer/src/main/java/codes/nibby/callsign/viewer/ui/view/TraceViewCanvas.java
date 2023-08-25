package codes.nibby.callsign.viewer.ui.view;

import codes.nibby.callsign.viewer.models.TraceDocument;
import codes.nibby.callsign.viewer.models.TraceDocumentAccessException;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

final class TraceViewCanvas extends Canvas {

    private final GraphicsContext graphics;

    public TraceViewCanvas() {
        this.graphics = getGraphicsContext2D();
    }

    public void paint(TraceDocument document) {
        graphics.clearRect(0, 0, getWidth(), getHeight());

        var entryCount = new AtomicInteger();

        try {
            document.streamEntries(new ArrayList<>(), entry -> {
                graphics.setFill(Color.BLUE);
                graphics.fillRect(entryCount.get() * 10, entryCount.get() * 10, 10, 10);
                entryCount.incrementAndGet();
            });
        } catch (TraceDocumentAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
