package codes.nibby.callsign.viewer.models.document;

import codes.nibby.callsign.viewer.models.filters.TraceFilters;
import codes.nibby.callsign.viewer.models.trace.Trace;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public interface TraceDocument {

    long UNDEFINED_START_TIME_MS = -1;
    long UNDEFINED_END_TIME_MS = -2;

    void load() throws TraceDocumentAccessException;

    void unload() throws TraceDocumentAccessException;

    List<String> getAllAttributeNames() throws TraceDocumentAccessException;

    void streamTraces(TraceFilters filters, Consumer<Trace> consumer) throws TraceDocumentAccessException;

    long getEarliestEventStartTimeMs();

    long getLatestEventEndTimeMs();

    Path getPath();

}
