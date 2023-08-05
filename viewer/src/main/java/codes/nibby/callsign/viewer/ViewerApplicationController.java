package codes.nibby.callsign.viewer;

import codes.nibby.callsign.viewer.models.TimelineDigestDocument;

public interface ViewerApplicationController {

    void openViewer(TimelineDigestDocument document);
    void closeViewer(TimelineDigestDocument document);

}
