package codes.nibby.callsign.viewer.ui.view;

import codes.nibby.callsign.viewer.models.trace.Trace;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.*;

final class TraceViewSidebar {

    private final Node component;

    private final ToolBar headerToolbar;

    private final TabPane tabPane;
    private final TraceSelectionTab traceSelectionTab;

    TraceViewSidebar() {
        BorderPane contentPane = new BorderPane();
        component = contentPane;

        headerToolbar = new ToolBar();
        headerToolbar.getItems().add(new Button("Test"));
        contentPane.setTop(headerToolbar);

        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        contentPane.setCenter(tabPane);

        traceSelectionTab = new TraceSelectionTab();
        tabPane.getTabs().addAll(
            new Tab("Interactions", traceSelectionTab.rootPane),
            new Tab("Perspective")
        );
    }

    public Node getComponent() {
        return component;
    }

    public void refreshContent(TraceViewSelection selection) {
        traceSelectionTab.refreshContent(selection);
    }

    private static final class TraceSelectionTab {

        private final BorderPane rootPane;
        private final VBox contentPane;

        private final TitledPane hoveredTraceDetailPane = new TitledPane();
        private final Map<Trace, TitledPane> selectionDetailPanes = new LinkedHashMap<>(4);

        public TraceSelectionTab() {
            rootPane = new BorderPane();

            contentPane = new VBox();
            contentPane.setFillWidth(true);

            var scrollPane = new ScrollPane(contentPane);
            scrollPane.setFitToHeight(true);
            scrollPane.setFitToWidth(true);
            rootPane.setCenter(scrollPane);

            hoveredTraceDetailPane.setAnimated(false);
            hoveredTraceDetailPane.setCollapsible(false);
            hoveredTraceDetailPane.setExpanded(true);
        }

        public void refreshContent(TraceViewSelection selection) {
            refreshHoveredContent(selection);
            refreshSelectionContent(selection);
        }

        private void refreshHoveredContent(TraceViewSelection selection) {
            Optional<Trace> hoveredTraceValue = selection.getHoveredTrace();

            if (hoveredTraceValue.isPresent() && !selection.getSelectedTraces().contains(hoveredTraceValue.get())) {
                Trace hoveredTrace = hoveredTraceValue.get();
                Node traceDetailContent = createTraceDetailContent(hoveredTrace);
                hoveredTraceDetailPane.setText("Hovered");
                hoveredTraceDetailPane.setContent(traceDetailContent);

                if (!contentPane.getChildren().contains(hoveredTraceDetailPane)) {
                    contentPane.getChildren().add(0, hoveredTraceDetailPane);
                }
            } else {
                hoveredTraceDetailPane.setContent(new Pane());
                contentPane.getChildren().remove(hoveredTraceDetailPane);
            }
        }

        private void refreshSelectionContent(TraceViewSelection selection) {
            Set<Trace> toRemove = new HashSet<>(selectionDetailPanes.keySet());

            for (Trace trace : selection.getSelectedTraces()) {
                toRemove.remove(trace);

                if (selectionDetailPanes.containsKey(trace)) {
                    continue;
                }

                var content = new TitledPane("Selected: " + trace.getName(), createTraceDetailContent(trace));
                contentPane.getChildren().add(content);

                selectionDetailPanes.put(trace, content);
            }

            for (Trace trace : toRemove) {
                TitledPane pane = selectionDetailPanes.remove(trace);
                contentPane.getChildren().remove(pane);
            }
        }

        private Node createTraceDetailContent(Trace trace) {
            TableView<AttributeRow> table = new TableView<>();

            TableColumn<AttributeRow, String> nameColumn = new TableColumn<>("Attribute");
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

            TableColumn<AttributeRow, String> valueColumn = new TableColumn<>("Value");
            valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));

            table.widthProperty().addListener(event -> {
                nameColumn.setPrefWidth(table.getWidth() / 2);
                valueColumn.setPrefWidth(table.getWidth() / 2);
            });

            table.getColumns().setAll(List.of(nameColumn, valueColumn));

            ObservableList<AttributeRow> tableData = FXCollections.observableArrayList(new ArrayList<>());
            table.setItems(tableData);

            Map<String, String> attributes = trace.getAttributes();

            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                var row = new AttributeRow(entry.getKey(), entry.getValue());
                tableData.add(row);
            }

            var content = new BorderPane(table);
            content.setMinHeight(350);

            return content;
        }

        public static final class AttributeRow {

            private final String name;
            private final String value;

            public AttributeRow(String name, String value) {
                this.name = name;
                this.value = value;
            }

            public String getName() {
                return name;
            }

            public String getValue() {
                return value;
            }
        }
    }
}
