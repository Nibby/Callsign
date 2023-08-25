package codes.nibby.callsign.viewer;

import codes.nibby.callsign.viewer.models.TraceDocument;
import codes.nibby.callsign.viewer.ui.UIHelper;
import codes.nibby.callsign.viewer.ui.view.TraceViewWindow;

import java.util.HashMap;
import java.util.Map;

final class ViewerApplicationControllerImpl implements ViewerApplicationController {

    private ViewerApplication application;
    private final Map<TraceDocument, TraceViewWindow> openWindows = new HashMap<>();

    ViewerApplicationControllerImpl(ViewerApplication application) {
        this.application = application;
    }

    @Override
    public void openTraceViewer(TraceDocument document) {
        UIHelper.runOnFxApplicationThread(() -> _openTraceViewer(document));
    }

    private void _openTraceViewer(TraceDocument document) {
        application.hideLandingScreen();

        TraceViewWindow window = openWindows.computeIfAbsent(document, key -> {
            var newWindow = new TraceViewWindow();
            newWindow.initialize(document);

            return newWindow;
        });

        window.setOnClose(() -> UIHelper.runOnFxApplicationThread(() ->
            nandleTraceViewerClosed(document)
        ));
        window.show();
    }

    @Override
    public void closeTraceViewer(TraceDocument document) {
        UIHelper.runOnFxApplicationThread(() -> _closeTraceViewer(document));
    }

    private void _closeTraceViewer(TraceDocument document) {
        nandleTraceViewerClosed(document);
    }

    private void nandleTraceViewerClosed(TraceDocument document) {
        openWindows.remove(document);

        if (openWindows.isEmpty()) {
            System.exit(0);
        }
    }
}
