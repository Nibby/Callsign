package codes.nibby.callsign.viewer;

import codes.nibby.callsign.viewer.models.TraceDocument;

public interface ViewerApplicationController {

    void openViewer(TraceDocument document);
    void closeViewer(TraceDocument document);

}
