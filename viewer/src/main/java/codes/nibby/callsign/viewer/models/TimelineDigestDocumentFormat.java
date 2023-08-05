package codes.nibby.callsign.viewer.models;

import codes.nibby.callsign.viewer.importer.SQLiteTimelineDigestDocumentAssembler;
import codes.nibby.callsign.viewer.importer.TimelineDigestDocumentAssembler;

import java.util.function.Supplier;

public enum TimelineDigestDocumentFormat {
    SQLITE(SQLiteTimelineDigestDocumentAssembler::new)

    ;

    private final Supplier<TimelineDigestDocumentAssembler> assemblerSupplier;

    TimelineDigestDocumentFormat(Supplier<TimelineDigestDocumentAssembler> assemblerSupplier) {
        this.assemblerSupplier = assemblerSupplier;
    }

    public TimelineDigestDocumentAssembler createAssembler() {
        return assemblerSupplier.get();
    }
}
