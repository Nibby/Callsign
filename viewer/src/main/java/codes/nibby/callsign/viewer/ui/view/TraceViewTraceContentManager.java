package codes.nibby.callsign.viewer.ui.view;

import codes.nibby.callsign.viewer.models.document.TraceDocument;
import codes.nibby.callsign.viewer.models.document.TraceDocumentAccessException;
import codes.nibby.callsign.viewer.models.filters.TraceFilters;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

final class TraceViewTraceContentManager {

    @Nullable
    private TraceContent traceContent = null;

    public TraceViewTraceContentManager() {
    }

    public TraceContent computeContent(
        TraceDocument document,
        @Nullable String trackBinningAttributeName,
        TraceFilters filters
    ) {
        if (trackBinningAttributeName == null) {
            traceContent = null;
            return null;
        }

        traceContent = new TraceContent(trackBinningAttributeName, trackBinningAttributeName);

        populateTraces(document, filters, traceContent);
        traceContent.computeDisplayData();

        return traceContent;
    }

    private void populateTraces(TraceDocument document, TraceFilters filters, TraceContent traceContent) {
        try {
            document.streamTraces(filters, traceContent::addTrace);
        } catch (TraceDocumentAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public TraceContent getLastComputedContent() {
        return traceContent;
    }
}
