package codes.nibby.callsign.viewer;

import codes.nibby.callsign.viewer.models.TraceDocument;

public interface ViewerApplicationController {

    void openTraceViewer(TraceDocument document);
    void closeTraceViewer(TraceDocument document);

}
