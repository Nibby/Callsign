package codes.nibby.callsign.viewer.ui.view;

import codes.nibby.callsign.viewer.models.document.TraceDocument;
import codes.nibby.callsign.viewer.models.document.TraceDocumentAccessException;
import codes.nibby.callsign.viewer.models.filters.TraceFilters;
import codes.nibby.callsign.viewer.models.trace.TraceTrack;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

final class TraceViewTraceContentManager {

    @Nullable
    private TraceCollection traceCollection = null;

    public TraceViewTraceContentManager() {
    }

    public TraceCollection computeContent(
        TraceDocument document,
        String trackBinningAttributeName,
        TraceFilters filters
    ) {
        if (traceCollection == null) {
            traceCollection = new TraceCollection(trackBinningAttributeName, trackBinningAttributeName);
        }

        populateTraces(document, filters, traceCollection);

        return traceCollection;
    }

    private void populateTraces(TraceDocument document, TraceFilters filters, TraceCollection traceCollection) {
        try {
            document.streamTraces(filters, traceCollection::addTrace);
        } catch (TraceDocumentAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
