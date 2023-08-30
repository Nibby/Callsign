package codes.nibby.callsign.viewer.models.document;

import codes.nibby.callsign.viewer.importer.SQLiteTraceDocumentAssembler;
import codes.nibby.callsign.viewer.importer.TraceDocumentAssembler;

import java.util.function.Supplier;

public enum TraceDocumentFormat {

    SQLITE(SQLiteTraceDocumentAssembler::new)

    ;

    private final Supplier<TraceDocumentAssembler> assemblerSupplier;

    TraceDocumentFormat(Supplier<TraceDocumentAssembler> assemblerSupplier) {
        this.assemblerSupplier = assemblerSupplier;
    }

    public TraceDocumentAssembler createAssembler() {
        return assemblerSupplier.get();
    }
}
