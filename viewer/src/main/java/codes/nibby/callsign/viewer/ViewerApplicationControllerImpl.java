package codes.nibby.callsign.viewer;

import codes.nibby.callsign.viewer.models.TimelineDigestDocument;
import codes.nibby.callsign.viewer.ui.UIHelper;

final class ViewerApplicationControllerImpl implements ViewerApplicationController {

    @Override
    public void openViewer(TimelineDigestDocument document) {
        UIHelper.runOnFxApplicationThread(() -> _openViewer(document));
    }

    private void _openViewer(TimelineDigestDocument document) {

    }

    @Override
    public void closeViewer(TimelineDigestDocument document) {
        UIHelper.runOnFxApplicationThread(() -> _closeViewer(document));
    }

    private void _closeViewer(TimelineDigestDocument document) {

    }

}
