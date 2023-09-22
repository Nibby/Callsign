package codes.nibby.callsign.viewer;

import codes.nibby.callsign.viewer.models.document.TraceDocument;

/**
 * Provides top-level operations supported by the viewer application.
 */
public interface ViewerApplicationController {

    void openTraceViewer(TraceDocument document);

    void closeTraceViewer(TraceDocument document);

}
