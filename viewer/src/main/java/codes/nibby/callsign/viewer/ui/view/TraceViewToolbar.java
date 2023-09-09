package codes.nibby.callsign.viewer.ui.view;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

public final class TraceViewToolbar {

    private final ToolBar toolbar;
    private final ComboBox<String> binningAttributesCombo;
    private final Spinner<Integer> zoomSpinner;

    TraceViewToolbar() {
        toolbar = new ToolBar();

        binningAttributesCombo = new ComboBox<>();
        binningAttributesCombo.getItems().addAll("index", "testAttr1");
        binningAttributesCombo.setPrefWidth(150);

        zoomSpinner = new Spinner<>(0, 100, 50);
        zoomSpinner.setEditable(true);
        zoomSpinner.setPrefWidth(80);

        var spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        toolbar.getItems().addAll(
            spacer,
            new Label("Bin Traces By"),
            binningAttributesCombo,
            gap(10),
            new Label("Zoom"),
            zoomSpinner,
            new Label("%")
        );

    }

    private Node gap(double space) {
        var gap = new Pane();
        HBox.setMargin(gap, new Insets(space/2));

        return gap;
    }

    public Node getComponent() {
        return toolbar;
    }
}
