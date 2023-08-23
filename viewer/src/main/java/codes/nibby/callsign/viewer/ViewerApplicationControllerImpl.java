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
    public void openExplorer(TraceDocument document) {
        UIHelper.runOnFxApplicationThread(() -> _openExplorer(document));
    }

    private void _openExplorer(TraceDocument document) {
        application.hideLandingScreen();

        TraceViewWindow window = openWindows.computeIfAbsent(document, key -> {
            var newWindow = new TraceViewWindow();
            newWindow.initialize(document);

            return newWindow;
        });

        window.setOnClose(() -> UIHelper.runOnFxApplicationThread(() ->
            handleExplorerWindowClosed(document)
        ));
        window.show();
    }

    @Override
    public void closeExplorer(TraceDocument document) {
        UIHelper.runOnFxApplicationThread(() -> _closeExplorer(document));
    }

    private void _closeExplorer(TraceDocument document) {
        handleExplorerWindowClosed(document);
    }

    private void handleExplorerWindowClosed(TraceDocument document) {
        openWindows.remove(document);

        if (openWindows.isEmpty()) {
            System.exit(0);
        }
    }
}
