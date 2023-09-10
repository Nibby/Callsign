package codes.nibby.callsign.viewer.ui.view;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;

public final class TraceViewToolbar {

    private final ToolBar toolbar;
    private final ComboBox<String> binningAttributesCombo;
    private final Spinner<Integer> zoomSpinner;

    private final CheckBox showIntervalTraces;
    private final CheckBox showInstantTraces;

    @Nullable
    private Consumer<String> binningAttributeChangeCallback;

    @Nullable
    private Consumer<Boolean> toggleShowInstantEventsCallback;

    @Nullable
    private Consumer<Boolean> toggleShowIntervalEventsCallback;

    @Nullable
    private Consumer<HorizontalZoom> zoomLevelChangeCallback;


    TraceViewToolbar() {
        toolbar = new ToolBar();

        binningAttributesCombo = new ComboBox<>();
        binningAttributesCombo.getItems().addAll("index", "testAttr1");
        binningAttributesCombo.setPrefWidth(150);
        binningAttributesCombo.getSelectionModel().selectedItemProperty().addListener(event -> {
            if (binningAttributeChangeCallback != null) {
                String selection = binningAttributesCombo.getSelectionModel().getSelectedItem();
                binningAttributeChangeCallback.accept(selection);
            }
        });

        zoomSpinner = new Spinner<>();
        zoomSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
            0,
            1800,
            100, // Initial amount
            10   // Amount to step by
        ));
        zoomSpinner.setEditable(true);
        zoomSpinner.setPrefWidth(80);
        zoomSpinner.valueProperty().addListener(event -> {
            if (zoomLevelChangeCallback != null) {
                double zoomLevel = zoomSpinner.getValue() / 100d;
                zoomLevelChangeCallback.accept(HorizontalZoom.of(zoomLevel));
            }
        });

        var spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        showInstantTraces = new CheckBox("Instances");
        showInstantTraces.setSelected(true);
        showInstantTraces.selectedProperty().addListener(event -> {
            if (toggleShowInstantEventsCallback != null) {
                toggleShowInstantEventsCallback.accept(showInstantTraces.isSelected());
            }
        });

        showIntervalTraces = new CheckBox("Intervals");
        showIntervalTraces.setSelected(true);
        showIntervalTraces.selectedProperty().addListener(event -> {
            if (toggleShowIntervalEventsCallback != null) {
                toggleShowIntervalEventsCallback.accept(showIntervalTraces.isSelected());
            }
        });

        toolbar.getItems().addAll(
            spacer,
            showInstantTraces,
            showIntervalTraces,
            gap(10),
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

    void notifyZoomLevelChanged(HorizontalZoom zoom) {
        int displayedZoomLevel = (int) (Math.round(zoom.value * 100));
        zoomSpinner.getValueFactory().setValue(displayedZoomLevel);
    }

    void notifyAvailableBinningAttributes(Collection<String> attributes) {
        Iterator<String> iterator = attributes.iterator();

        binningAttributesCombo.getItems().setAll(attributes);

        if (iterator.hasNext()){
            binningAttributesCombo.getSelectionModel().select(iterator.next());
        }
    }

    public void setBinningAttributeChangeCallback(@Nullable Consumer<String> binningAttributeChangeCallback) {
        this.binningAttributeChangeCallback = binningAttributeChangeCallback;
    }

    public void setToggleShowInstantEventsCallback(@Nullable Consumer<Boolean> toggleShowInstantEventsCallback) {
        this.toggleShowInstantEventsCallback = toggleShowInstantEventsCallback;
    }

    public void setToggleShowIntervalEventsCallback(@Nullable Consumer<Boolean> toggleShowIntervalEventsCallback) {
        this.toggleShowIntervalEventsCallback = toggleShowIntervalEventsCallback;
    }

    public void setZoomLevelChangeCallback(@Nullable Consumer<HorizontalZoom> zoomLevelChangeCallback) {
        this.zoomLevelChangeCallback = zoomLevelChangeCallback;
    }
}
