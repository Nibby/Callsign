package codes.nibby.callsign.viewer.ui.view.visual;

import javafx.geometry.Rectangle2D;

public interface Element {

    Rectangle2D getBoundingBox();

    byte getInteractionPriority();

}
