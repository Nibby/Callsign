package codes.nibby.callsign.viewer.importer;

import codes.nibby.callsign.api.Event;
import codes.nibby.callsign.viewer.ProgressReporter;
import codes.nibby.callsign.viewer.models.SQLiteTimelineDigestDocument;
import codes.nibby.callsign.viewer.models.TimelineDigestDocument;
import codes.nibby.callsign.viewer.models.WritableSQLiteTimelineDigestDocument;
import codes.nibby.callsign.viewer.models.WritableTimelineDigestDocument;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public final class SQLiteTimelineDigestDocumentAssembler implements TimelineDigestDocumentAssembler {

    @Override
    public TimelineDigestDocument assemble(AssemblyOptions options, ProgressReporter progressReporter) throws IOException {
        long totalEventCount = performInitialPass(options.inputTraceFiles, progressReporter);

        WritableTimelineDigestDocument document = createDigestDocument(options.outputFile, progressReporter);

        importTraceData(document, options.inputTraceFiles, totalEventCount, progressReporter);

        return new SQLiteTimelineDigestDocument(options.outputFile);
    }

    private long performInitialPass(List<InputTraceFile> inputTraceFiles, ProgressReporter progressReporter) throws IOException {
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

    private long countEventEntries(InputTraceFile inputFile) throws IOException {
        var counter = new AtomicInteger(0);

        inputFile.streamEventData(event -> counter.incrementAndGet());

        return counter.get();
    }

    private WritableTimelineDigestDocument createDigestDocument(Path outputFile, ProgressReporter progressReporter) throws IOException {
        var document = new WritableSQLiteTimelineDigestDocument(outputFile);
        document.loadForWrite();

        return document;
    }

    private void importTraceData(WritableTimelineDigestDocument document, List<InputTraceFile> inputTraceFiles, long totalEventCount, ProgressReporter progressReporter) throws IOException {
        for (InputTraceFile traceFile : inputTraceFiles) {
            traceFile.streamEventData(event -> importTraceEvent(event, document, totalEventCount, progressReporter));
        }
    }

    private void importTraceEvent(Event event, WritableTimelineDigestDocument document, long totalEventCount, ProgressReporter progressReporter) {
        document.appendEvent(event);

        // TODO: Progress reporting
    }
}
