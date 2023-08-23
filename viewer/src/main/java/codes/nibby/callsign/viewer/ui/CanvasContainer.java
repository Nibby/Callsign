package codes.nibby.callsign.viewer.ui;

import javafx.geometry.Dimension2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public final class CanvasContainer extends Pane {

    private final List<Canvas> canvas;
    private final List<Consumer<Dimension2D>> canvasSizeListeners = new ArrayList<>();

    public CanvasContainer(Canvas canvas) {
        this(Collections.singletonList(canvas));
    }

    public CanvasContainer(Collection<? extends Canvas> canvas) {
        this.canvas = List.copyOf(canvas);
        this.getChildren().addAll(canvas);
    }

    public void addSizeUpdateListener(Consumer<Dimension2D> listener) {
        canvasSizeListeners.add(listener);
    }

    public void removeSizeUpdateListener(Consumer<Dimension2D> listener) {
        canvasSizeListeners.remove(listener);
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();

        resizeCanvas();
    }

    private void resizeCanvas() {
        final double x = snappedLeftInset();
        final double y = snappedTopInset();
        final double w = snapSizeX(getWidth()) - x - snappedRightInset();
        final double h = snapSizeY(getHeight()) - y - snappedBottomInset();

        canvas.forEach(c -> {
            c.setLayoutX(x);
            c.setLayoutY(y);
            c.setWidth(w);
            c.setHeight(h);
        });

        var size = new Dimension2D(w, h);
        canvasSizeListeners.forEach(listener -> listener.accept(size));
    }
}
