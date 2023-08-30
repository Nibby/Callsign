package codes.nibby.callsign.viewer.models.document;

import codes.nibby.callsign.viewer.models.filters.TraceFilter;
import codes.nibby.callsign.viewer.models.trace.Trace;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public interface TraceDocument {

    long UNDEFINED_START_TIME_NS = Long.MAX_VALUE;
    long UNDEFINED_END_TIME_NS = Long.MIN_VALUE;

    void load() throws TraceDocumentAccessException;

    void unload() throws TraceDocumentAccessException;

    void streamTraces(List<TraceFilter> filters, Consumer<Trace> consumer) throws TraceDocumentAccessException;

    long getEarliestEventStartTimeNs();

    long getLatestEventEndTimeNs();

    Path getPath();

}
