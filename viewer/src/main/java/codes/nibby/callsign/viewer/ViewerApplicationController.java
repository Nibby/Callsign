package codes.nibby.callsign.viewer;

import codes.nibby.callsign.viewer.models.TraceDocument;

public interface ViewerApplicationController {

    void openExplorer(TraceDocument document);
    void closeExplorer(TraceDocument document);

}
