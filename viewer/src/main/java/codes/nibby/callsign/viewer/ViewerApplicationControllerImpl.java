package codes.nibby.callsign.viewer;

import codes.nibby.callsign.viewer.models.TraceDocument;
import codes.nibby.callsign.viewer.ui.UIHelper;

final class ViewerApplicationControllerImpl implements ViewerApplicationController {

    @Override
    public void openViewer(TraceDocument document) {
        UIHelper.runOnFxApplicationThread(() -> _openViewer(document));
    }

    private void _openViewer(TraceDocument document) {

    }

    @Override
    public void closeViewer(TraceDocument document) {
        UIHelper.runOnFxApplicationThread(() -> _closeViewer(document));
    }

    private void _closeViewer(TraceDocument document) {

    }

}
