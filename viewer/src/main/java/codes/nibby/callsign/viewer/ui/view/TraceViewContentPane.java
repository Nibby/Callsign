package codes.nibby.callsign.viewer.ui.view;

import codes.nibby.callsign.viewer.models.document.TraceDocument;
import codes.nibby.callsign.viewer.models.document.TraceDocumentAccessException;
import codes.nibby.callsign.viewer.models.filters.TraceFilters;
import codes.nibby.callsign.viewer.ui.CanvasContainer;
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class TraceViewContentPane {

    private final BorderPane rootPane;
    private final BorderPane contentPane;

    private TraceDocument document;

    private final TraceViewToolbar toolbar;

    private final TraceViewPerspectiveManager perspective;
    private final TraceViewTraceContentManager contentManager;
    private final TraceViewColorScheme colorScheme;
    private final TraceFilters traceFilters;

    private final TraceViewCanvas canvas;
    private final ScrollBar canvasHorizontalScroll;
    private final ScrollBar canvasVerticalScroll;

    private String binningAttribute = null;
    private boolean showInstantEvents = true;
    private boolean showIntervalEvents = true;

    public TraceViewContentPane() {
        rootPane = new BorderPane();

        toolbar = new TraceViewToolbar();
        toolbar.setBinningAttributeChangeCallback(this::handleBinningAttributeChanged);
        toolbar.setToggleShowInstantEventsCallback(this::handleShowInstantEventSettingChanged);
        toolbar.setToggleShowIntervalEventsCallback(this::handleShowIntervalEventSettingChanged);
        toolbar.setZoomLevelChangeCallback(this::handleHorizontalZoomLevelChanged);

        rootPane.setTop(toolbar.getComponent());

        contentPane = new BorderPane();
        rootPane.setCenter(contentPane);

        traceFilters = new TraceFilters();
        colorScheme = new TraceViewLightColorScheme();
        contentManager = new TraceViewTraceContentManager();
        perspective = new TraceViewPerspectiveManager();

        canvas = new TraceViewCanvas();
        canvas.setOnScroll(this::handleCanvasScroll);
        var canvasContainer = new CanvasContainer(canvas);
        canvasContainer.addSizeUpdateListener(newSize -> refreshContent());

        BorderPane canvasContent = new BorderPane();
        canvasContent.setCenter(canvasContainer);

        canvasVerticalScroll = new ScrollBar();
        canvasVerticalScroll.setOrientation(Orientation.VERTICAL);
        canvasVerticalScroll.setMin(0);
        canvasVerticalScroll.setMax(0);
        canvasVerticalScroll.setValue(0);
        canvasVerticalScroll.setDisable(true);
        canvasVerticalScroll.setUnitIncrement(30);
        canvasVerticalScroll.setBlockIncrement(100);
        canvasVerticalScroll.valueProperty().addListener(event -> {
            perspective.setDisplayOffsetY(canvasVerticalScroll.getValue());
            refreshContent();
        });
        canvasContent.setRight(canvasVerticalScroll);

        canvasHorizontalScroll = new ScrollBar();
        canvasHorizontalScroll.setOrientation(Orientation.HORIZONTAL);
        canvasHorizontalScroll.setMin(0);
        canvasHorizontalScroll.setMax(0);
        canvasHorizontalScroll.setValue(0);
        canvasHorizontalScroll.setDisable(true);
        canvasHorizontalScroll.valueProperty().addListener(event -> {
            perspective.setDisplayOffsetTimeNs((long) canvasHorizontalScroll.getValue());
            refreshContent();
        });
        BorderPane horizontalScrollPane = new BorderPane();
        horizontalScrollPane.setCenter(canvasHorizontalScroll);
        var gapPane = new Pane();
        gapPane.setPrefWidth(16);
        horizontalScrollPane.setRight(gapPane);
        canvasContent.setBottom(horizontalScrollPane);

        contentPane.setCenter(canvasContent);
    }

    private void handleShowIntervalEventSettingChanged(boolean show) {
        if (showIntervalEvents != show) {
            showIntervalEvents = show;
            refreshContent();
        }
    }

    private void handleShowInstantEventSettingChanged(boolean show) {
        if (showInstantEvents != show) {
            showInstantEvents = show;
            refreshContent();
        }
    }

    private void handleBinningAttributeChanged(String newBinningAttribute) {
        if (!Objects.equals(this.binningAttribute, newBinningAttribute)) {
            this.binningAttribute = newBinningAttribute;
        }

        refreshContent();
    }

    private void handleHorizontalZoomLevelChanged(double newZoomLevel) {
        if (Math.abs(perspective.getTrackHorizontalZoomLevel() - newZoomLevel) > 0.0001d) {
            perspective.setHorizontalZoom(newZoomLevel);
            refreshContent();
        }
    }

    private void handleCanvasScroll(ScrollEvent event) {
        if (event.isShiftDown()) {
            // Horizontal scroll left/right
            if (!canvasHorizontalScroll.isDisabled()) {
                canvasHorizontalScroll.adjustValue(-(event.getTotalDeltaX()));
            }
        } else if (event.isAltDown()) {
            // Horizontal zoom in/out
            double zoom = perspective.getTrackHorizontalZoomLevel();

            if (event.getTotalDeltaY() > 0) {
                adjustZoom(0.5d);
            } else {
                adjustZoom(-0.5d);
            }
        } else {
            // Vertical scroll up/down
            if (!canvasVerticalScroll.isDisabled()) {
                canvasVerticalScroll.adjustValue(-(event.getTotalDeltaY()));
            }
        }
    }

    private void adjustZoom(double amount) {
        double currentZoom = perspective.getTrackHorizontalZoomLevel();
        perspective.setHorizontalZoom(currentZoom + amount);
        toolbar.notifyZoomLevelChanged(currentZoom + amount);

        refreshContent();
    }

    private void refreshContent() {
        double totalWidth = contentPane.getWidth() - canvasVerticalScroll.getWidth();
        double totalHeight = contentPane.getHeight() - canvasHorizontalScroll.getHeight();

        long earliestEventTimeNs = document.getEarliestEventStartTimeNs();
        long latestEventTimeNs = document.getLatestEventEndTimeNs();

        boolean viewportChanged = perspective.applyProperties(totalWidth, totalHeight, earliestEventTimeNs, latestEventTimeNs);

        @Nullable TraceContent traces;

        if (binningAttribute != null) {
            if (viewportChanged) {
                long displayedEarliestTimeNs = perspective.getDisplayedEarliestEventTimeNs();
                long displayedLatestTimeNs = perspective.getDisplayedLatestEventTimeNs();

                traceFilters.setDisplayedTimeInterval(displayedEarliestTimeNs, displayedLatestTimeNs);

                traces = contentManager.computeContent(document, binningAttribute, traceFilters);
            } else {
                traces = Objects.requireNonNull(contentManager.getLastComputedContent(), "no last computed content");
            }

            updateScrollbars(traces);
        } else {
            traces = null;
        }

        canvas.paint(perspective, traces, colorScheme);
    }

    private void updateScrollbars(TraceContent traces) {
        updateVerticalScrollbar(traces);
        updateHorizontalScrollbar(traces);
    }

    private void updateHorizontalScrollbar(TraceContent traces) {
        Long earliestTimeNs = traces.getEarliestTraceEventStartNs();
        Long latestTimeNs = traces.getLatestTraceEventEndNs();

        boolean canScroll;

        if (earliestTimeNs == null || latestTimeNs == null) {
            canScroll = false;
        } else {
            long totalTimeIntervalNs = latestTimeNs - earliestTimeNs;
            long displayedTimeIntervalNs = perspective.getDisplayedLatestEventTimeNs() - perspective.getDisplayedEarliestEventTimeNs();

            canScroll = totalTimeIntervalNs - displayedTimeIntervalNs > 0;

            if (canScroll) {
                var totalAmountScrollable = totalTimeIntervalNs - displayedTimeIntervalNs;
                var visibleAmount = (displayedTimeIntervalNs / (double) totalTimeIntervalNs) * totalAmountScrollable;

                canvasHorizontalScroll.setMax(totalAmountScrollable);
                canvasHorizontalScroll.setVisibleAmount(visibleAmount);
                canvasHorizontalScroll.setUnitIncrement(totalAmountScrollable / 30d);
                canvasHorizontalScroll.setBlockIncrement(totalAmountScrollable / 100d);
            }
        }

        if (!canScroll) {
            canvasHorizontalScroll.setMax(0);
            canvasHorizontalScroll.setValue(0);
            canvasHorizontalScroll.setVisibleAmount(0);
        }

        canvasHorizontalScroll.setDisable(!canScroll);
    }

    private void updateVerticalScrollbar(TraceContent traces) {
        double totalViewableHeight = traces.getDisplayData().getTotalBands() * perspective.getTrackBandHeight();
        double viewportHeight = perspective.getViewportHeight();

        boolean canScroll = (totalViewableHeight - viewportHeight) > 0;

        canvasVerticalScroll.setDisable(!canScroll);

        if (canScroll) {
            var totalAmountScrollable = totalViewableHeight - viewportHeight;
            var visibleAmount = (viewportHeight / totalViewableHeight) * totalAmountScrollable;

            canvasVerticalScroll.setMax(totalAmountScrollable);
            canvasVerticalScroll.setVisibleAmount(visibleAmount);
        } else {
            canvasVerticalScroll.setMax(0);
            canvasVerticalScroll.setVisibleAmount(0);
            canvasVerticalScroll.setValue(0);
        }
    }

    public BorderPane getComponent() {
        return rootPane;
    }

    public void setDocument(TraceDocument document) {
        this.document = document;

        try {
            this.toolbar.notifyAvailableBinningAttributes(document.getAllAttributeNames());
        } catch (TraceDocumentAccessException e) {
            throw new RuntimeException(e);
        }

        refreshContent();
    }
}
