package codes.nibby.callsign.viewer.ui.view;

import codes.nibby.callsign.viewer.models.TraceDocument;
import org.jetbrains.annotations.Nullable;

final class TraceViewContentManager {

    @Nullable
    private TraceEventCollection lastComputedCollection = null;

    public TraceViewContentManager() {

    }

    public TraceEventCollection compute(String trackAttributeName, TraceDocument document) {
        lastComputedCollection = new TraceEventCollection();
        lastComputedCollection.compute(trackAttributeName, document);

        return lastComputedCollection;
    }

    public TraceEventCollection getLastComputedTraceEvents() {
        return lastComputedCollection;
    }

}
