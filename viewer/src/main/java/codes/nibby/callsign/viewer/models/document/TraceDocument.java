package codes.nibby.callsign.viewer.models.document;

import codes.nibby.callsign.viewer.models.filters.TraceFilters;
import codes.nibby.callsign.viewer.models.trace.Trace;

import java.nio.file.Path;
import java.util.function.Consumer;

public interface TraceDocument {

    long UNDEFINED_START_TIME_NS = -1;
    long UNDEFINED_END_TIME_NS = -2;

    void load() throws TraceDocumentAccessException;

    void unload() throws TraceDocumentAccessException;

    void streamTraces(TraceFilters filters, Consumer<Trace> consumer) throws TraceDocumentAccessException;

    long getEarliestEventStartTimeNs();

    long getLatestEventEndTimeNs();

    Path getPath();

}
