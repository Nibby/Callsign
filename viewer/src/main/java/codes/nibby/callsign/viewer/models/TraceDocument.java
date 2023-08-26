package codes.nibby.callsign.viewer.models;

import java.util.List;
import java.util.function.Consumer;

public interface TraceDocument {

    void load() throws TraceDocumentAccessException;

    void unload() throws TraceDocumentAccessException;

    void streamEntries(List<TraceEntryFilter> filters, Consumer<TraceEvent> consumer) throws TraceDocumentAccessException;

    long getEarliestEventStartTimeNs();

    long getLatestEventEndTimeNs();

}
