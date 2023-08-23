package codes.nibby.callsign.viewer.ui.view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

// TODO: Question if using the SceneGraph directly is easier to work with (need to understand it better)
final class TraceViewCanvas extends Canvas {

    private final GraphicsContext graphics;

    public TraceViewCanvas() {
        this.graphics = getGraphicsContext2D();
    }

    public void paint(TraceViewPaintParameters paintParameters) {
        graphics.setFill(Color.BLACK);
        graphics.fillRect(0, 0, getWidth(), getHeight());

    }
}
