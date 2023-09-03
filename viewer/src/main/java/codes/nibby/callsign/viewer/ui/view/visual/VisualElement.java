package codes.nibby.callsign.viewer.ui.view.visual;

import javafx.geometry.Rectangle2D;

public final class VisualElement<T> implements Element {

    private final byte interactionPriority;
    private final T data;
    private final Rectangle2D boundingBox;

    public VisualElement(byte interactionPriority, T data, Rectangle2D boundingBox) {
        this.interactionPriority = interactionPriority;
        this.data = data;
        this.boundingBox = boundingBox;
    }

    @Override
    public Rectangle2D getBoundingBox() {
        return boundingBox;
    }

    @Override
    public byte getInteractionPriority() {
        return interactionPriority;
    }

    public T getData() {
        return data;
    }
}
