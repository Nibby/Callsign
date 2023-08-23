package codes.nibby.callsign.viewer.importer;

import codes.nibby.callsign.api.Event;
import codes.nibby.callsign.viewer.misc.ProgressReporter;
import codes.nibby.callsign.viewer.models.SQLiteTraceDocument;
import codes.nibby.callsign.viewer.models.TraceDocument;
import codes.nibby.callsign.viewer.models.WritableSQLiteTraceDocument;
import codes.nibby.callsign.viewer.models.WritableTraceDocument;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public final class SQLiteTraceDocumentAssembler implements TraceDocumentAssembler {

    @Override
    public TraceDocument assemble(AssemblyOptions options, ProgressReporter progressReporter) throws IOException {
        long totalEventCount = performInitialPass(options.inputTraceFiles, progressReporter);

        WritableTraceDocument document = createDigestDocument(options.outputFile, progressReporter);

        importTraceData(document, options.inputTraceFiles, totalEventCount, progressReporter);

        return new SQLiteTraceDocument(options.outputFile);
    }

    private long performInitialPass(List<RawTraceFile> inputTraceFiles, ProgressReporter progressReporter) throws IOException {
        progressReporter.notifyProgressMessageChanged("Pre-processing...");
        progressReporter.notifyProgressIndeterminate(true);

        // TODO: Handle failures better
        List<IOException> failures = new ArrayList<>();

        Optional<Long> totalEventCount = inputTraceFiles.parallelStream()
            .map(traceFile -> {
                try {
                    return countEventEntries(traceFile);
                } catch (IOException e) {
                    synchronized (failures) {
                        failures.add(e);
                    }
                    return 0L;
                }
            })
            .reduce(Long::sum);

        if (!failures.isEmpty()) {
            throw new IOException(failures.size() + " exceptions occurred during initial pass! Showing cause of first failure", failures.get(0));
        }

        return totalEventCount.orElse(0L);
    }

    private long countEventEntries(RawTraceFile inputFile) throws IOException {
        var counter = new AtomicInteger(0);

        inputFile.streamEventData(event -> counter.incrementAndGet());

        return counter.get();
    }

    private WritableTraceDocument createDigestDocument(Path outputFile, ProgressReporter progressReporter) throws IOException {
        var document = new WritableSQLiteTraceDocument(outputFile);
        document.initialize();

        return document;
    }

    private void importTraceData(WritableTraceDocument document, List<RawTraceFile> inputTraceFiles, long totalEventCount, ProgressReporter progressReporter) throws IOException {
        for (RawTraceFile traceFile : inputTraceFiles) {
            traceFile.streamEventData(event -> importTraceEvent(event, document, totalEventCount, progressReporter));
        }
    }

    private void importTraceEvent(Event event, WritableTraceDocument document, long totalEventCount, ProgressReporter progressReporter) {
        try {
            document.appendEvent(event);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // TODO: Progress reporting
    }
}
