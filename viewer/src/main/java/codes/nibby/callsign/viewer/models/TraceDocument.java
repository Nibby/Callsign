package codes.nibby.callsign.viewer.models;

import java.util.List;
import java.util.function.Consumer;

public interface TraceDocument {

    void load() throws TraceDocumentAccessException;

    void unload() throws TraceDocumentAccessException;

    void streamEntries(List<TraceEntryFilter> filters, Consumer<TraceEntry> consumer) throws TraceDocumentAccessException;

}
