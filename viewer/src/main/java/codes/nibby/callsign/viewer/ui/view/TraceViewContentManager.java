package codes.nibby.callsign.viewer.ui.view;

import codes.nibby.callsign.viewer.models.TraceDocument;
import org.jetbrains.annotations.Nullable;

final class TraceViewContentManager {

    @Nullable
    private TraceCollection lastComputedCollection = null;

    public TraceViewContentManager() {

    }

    public TraceCollection compute(String trackAttributeName, TraceDocument document) {
        lastComputedCollection = new TraceCollection();
        lastComputedCollection.compute(trackAttributeName, document);

        return lastComputedCollection;
    }

    public TraceCollection getLastComputedTraceEvents() {
        return lastComputedCollection;
    }

}
