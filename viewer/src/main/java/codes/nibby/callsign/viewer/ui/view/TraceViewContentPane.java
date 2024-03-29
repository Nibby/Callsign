package codes.nibby.callsign.viewer.ui.view;

import codes.nibby.callsign.viewer.models.document.TraceDocument;
import codes.nibby.callsign.viewer.models.document.TraceDocumentAccessException;
import codes.nibby.callsign.viewer.models.filters.TraceFilters;
import codes.nibby.callsign.viewer.ui.CanvasContainer;
import com.google.common.base.Preconditions;
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SplitPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public final class TraceViewContentPane {

    private final BorderPane rootPane;
    private final BorderPane contentPane;

    private TraceDocument document;

    private final TraceViewToolbar toolbar;
    private final TraceViewSidebar sidebar;

    private final TraceViewViewportManager viewport;
    private final TraceViewSelectionManager selection;
    private final TraceViewTraceContentGenerator contentGenerator;
    private final TraceViewDisplayOptions displayOptions;

    private final TraceViewCanvas canvas;
    private final TraceViewCanvasInputHandler canvasInputHandler;
    private final ScrollBar canvasHorizontalScroll;
    private final ScrollBar canvasVerticalScroll;

    public TraceViewContentPane() {
        rootPane = new BorderPane();

        toolbar = new TraceViewToolbar();
        toolbar.setBinningAttributeChangeCallback(this::handleBinningAttributeNameChanged);
        toolbar.setToggleShowInstantEventsCallback(this::handleShowInstantEventSettingChanged);
        toolbar.setToggleShowIntervalEventsCallback(this::handleShowIntervalEventSettingChanged);
        toolbar.setZoomLevelChangeCallback(this::handleHorizontalZoomLevelChanged);

        rootPane.setTop(toolbar.getComponent());

        contentPane = new BorderPane();
        rootPane.setCenter(contentPane);

        displayOptions = new TraceViewDisplayOptions();
        contentGenerator = new TraceViewTraceContentGenerator();
        viewport = new TraceViewViewportManager();
        selection = new TraceViewSelectionManager();

        canvas = new TraceViewCanvas();
        canvas.setOnScroll(this::handleCanvasScroll);
        var canvasContainer = new CanvasContainer(canvas);
        canvasContainer.addSizeUpdateListener(newSize -> refreshContent());

        canvasInputHandler = new TraceViewCanvasInputHandler();
        canvas.setOnMouseMoved(event -> {
            canvasInputHandler.handleMouseMove(
                event,
                viewport,
                selection,
                contentGenerator.getLastComputedContent()
            );
            refreshContent();
        });
        canvas.setOnMousePressed(event -> {
            canvasInputHandler.handleMousePress(
                event,
                viewport,
                selection,
                contentGenerator.getLastComputedContent()
            );
            refreshContent();
        });
        canvas.setOnMouseExited(event -> {
            canvasInputHandler.handleMouseExit(
                event,
                viewport,
                selection
            );
            refreshContent();
        });

        BorderPane canvasContent = new BorderPane();
        canvasContent.setCenter(canvasContainer);

        canvasVerticalScroll = new ScrollBar();
        canvasVerticalScroll.setOrientation(Orientation.VERTICAL);
        canvasVerticalScroll.setMin(0);
        canvasVerticalScroll.setMax(0);
        canvasVerticalScroll.setValue(0);
        canvasVerticalScroll.setDisable(true);
        canvasVerticalScroll.setUnitIncrement(15);
        canvasVerticalScroll.setBlockIncrement(100);
        canvasVerticalScroll.valueProperty().addListener(event -> {
            viewport.setTrackContentOffsetY(canvasVerticalScroll.getValue());
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
            viewport.setTrackContentTimeOffsetMs((long) canvasHorizontalScroll.getValue());
            refreshContent();
        });
        BorderPane horizontalScrollPane = new BorderPane();
        horizontalScrollPane.setCenter(canvasHorizontalScroll);
        var gapPane = new Pane();
        gapPane.setPrefWidth(16);
        horizontalScrollPane.setRight(gapPane);
        canvasContent.setBottom(horizontalScrollPane);

        BorderPane sidebarContent = new BorderPane();

        sidebar = new TraceViewSidebar();
        sidebarContent.setCenter(sidebar.getComponent());

        SplitPane contentSidebarSplit = new SplitPane(canvasContent, sidebarContent);
        contentSidebarSplit.getDividers().get(0).setPosition(0.75d);

        contentPane.setCenter(contentSidebarSplit);
    }

    private void handleShowIntervalEventSettingChanged(boolean show) {
        if (displayOptions.isShowIntervalTraces() != show) {
            displayOptions.setShowIntervalTraces(show);
            refreshContent();
        }
    }

    private void handleShowInstantEventSettingChanged(boolean show) {
        if (displayOptions.isShowInstantTraces() != show) {
            displayOptions.setShowInstantTraces(show);
            refreshContent();
        }
    }

    private void handleBinningAttributeNameChanged(String newBinningAttributeName) {
        if (!Objects.equals(displayOptions.getBinningAttributeName(), newBinningAttributeName)) {
            displayOptions.setBinningAttributeName(newBinningAttributeName);
            refreshContent();
        }
    }

    private void handleHorizontalZoomLevelChanged(HorizontalZoom newZoomLevel) {
        Preconditions.checkNotNull(newZoomLevel);

        if (!Objects.equals(newZoomLevel, viewport.getTrackHorizontalZoom())) {
            viewport.setZoom(newZoomLevel);
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
        HorizontalZoom currentZoom = viewport.getTrackHorizontalZoom();
        HorizontalZoom newZoom = currentZoom.adjust(amount);

        viewport.setZoom(newZoom);
        toolbar.notifyZoomLevelChanged(newZoom);

        refreshContent();
    }

    // TODO: Temporary
    private String lastBinningAttributeName = null;

    private void refreshContent() {
        double totalWidth = contentPane.getWidth() - canvasVerticalScroll.getWidth();
        double totalHeight = contentPane.getHeight() - canvasHorizontalScroll.getHeight();

        long earliestEventTimeMs = document.getEarliestEventStartTimeMs();
        long latestEventTimeMs = document.getLatestEventEndTimeMs();

        boolean viewportChanged = viewport.applyProperties(totalWidth, totalHeight, earliestEventTimeMs, latestEventTimeMs);

        @Nullable TraceContent traces;

        String binningAttributeName = displayOptions.getBinningAttributeName();

        if (binningAttributeName != null) {
            if (viewportChanged || !Objects.equals(lastBinningAttributeName, binningAttributeName)) {
                long displayedEarliestTimeMs = viewport.getDisplayedEarliestEventTimeMs();
                long displayedLatestTimeMs = viewport.getDisplayedLatestEventTimeMs();

                TraceFilters filters = displayOptions.getFilters();
                filters.setDisplayedTimeInterval(displayedEarliestTimeMs, displayedLatestTimeMs);

                traces = contentGenerator.computeContent(document, binningAttributeName, filters);
                lastBinningAttributeName = binningAttributeName;
            } else {
                traces = Objects.requireNonNull(contentGenerator.getLastComputedContent(), "no last computed content");
            }

            updateScrollbars(traces);
        } else {
            traces = null;
        }

        sidebar.refreshContent(selection);

        canvas.paint(viewport, traces, selection, displayOptions);
    }

    private void updateScrollbars(TraceContent traces) {
        updateVerticalScrollbar(traces);
        updateHorizontalScrollbar(traces);
    }

    private void updateHorizontalScrollbar(TraceContent traces) {
        Long earliestTimeMs = traces.getEarliestTraceEventStartMs();
        Long latestTimeMs = traces.getLatestTraceEventEndMs();

        boolean canScroll;

        if (earliestTimeMs == null || latestTimeMs == null) {
            canScroll = false;
        } else {
            long totalTimeIntervalMs = latestTimeMs - earliestTimeMs;
            long displayedTimeIntervalMs = viewport.getDisplayedLatestEventTimeMs() - viewport.getDisplayedEarliestEventTimeMs();

            canScroll = totalTimeIntervalMs - displayedTimeIntervalMs > 0;

            if (canScroll) {
                var totalAmountScrollable = totalTimeIntervalMs - displayedTimeIntervalMs;
                var visibleAmount = (displayedTimeIntervalMs / (double) totalTimeIntervalMs) * totalAmountScrollable;

                canvasHorizontalScroll.setMax(totalAmountScrollable);
                canvasHorizontalScroll.setVisibleAmount(visibleAmount);
                canvasHorizontalScroll.setUnitIncrement(totalAmountScrollable / 30d);
                canvasHorizontalScroll.setBlockIncrement(Math.max(TimeUnit.SECONDS.toMillis(1), totalAmountScrollable / 100d));
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
        double totalViewableHeight = traces.getTotalDisplayableBands() * viewport.getTrackBandHeight();
        double traceContentHeight = viewport.getTrackContentBounds().getHeight();

        boolean canScroll = (totalViewableHeight - traceContentHeight) > 0;

        canvasVerticalScroll.setDisable(!canScroll);

        if (canScroll) {
            var totalAmountScrollable = totalViewableHeight - traceContentHeight;
            var visibleAmount = (traceContentHeight / totalViewableHeight) * totalAmountScrollable;

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
