package codes.nibby.callsign.viewer.models;

import codes.nibby.callsign.api.Event;
import codes.nibby.callsign.viewer.TestDataGenerator;
import codes.nibby.callsign.viewer.TestHelper;
import codes.nibby.callsign.viewer.models.document.TraceDocument;
import codes.nibby.callsign.viewer.models.document.TraceDocumentAccessException;
import codes.nibby.callsign.viewer.models.document.WritableTraceDocument;
import codes.nibby.callsign.viewer.models.filters.TraceFilters;
import codes.nibby.callsign.viewer.models.trace.InstantTrace;
import codes.nibby.callsign.viewer.models.trace.IntervalTrace;
import codes.nibby.callsign.viewer.models.trace.Trace;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public abstract class WritableTraceDocumentTest {

    private final Path testDir;

    public WritableTraceDocumentTest() throws IOException {
        testDir = TestHelper.createTestDataDirectory();
    }

    @AfterEach
    public void cleanUp() throws IOException {
        if (Files.exists(testDir)) {
            TestHelper.deleteRecursive(testDir);
        }
    }

    protected abstract WritableTraceDocument createWritableInstance(Path testDir) throws IOException;

    protected abstract TraceDocument createInstance(Path traceDigestFile) throws TraceDocumentAccessException;

    private WritableTraceDocument createWritableInstance() throws IOException {
        return createWritableInstance(this.testDir);
    }

    @Test
    public void testAppendThenStream_instantEvent_single() throws Exception {
        var writableDocument = createWritableInstance();

        var event = TestDataGenerator.generateSingleInstantEvent().asList().get(0);
        writableDocument.appendEvent(event);

        testReadBack(writableDocument, document -> {
            List<Trace> traces = new ArrayList<>();
            document.streamTraces(new TraceFilters(), traces::add);

            assertEquals(1, traces.size());

            var trace = traces.get(0);

            assertInstanceOf(InstantTrace.class, trace);
            assertEquals(event.getTimeNs(), document.getEarliestEventStartTimeNs());
            assertEquals(event.getTimeNs(), document.getLatestEventEndTimeNs());

            assertAttributeEquals(event, trace);
        });
    }

    @Test
    public void testAppendThenStream_intervalStartEvent_traceEventHasNoEndTime() throws Exception {
        var writableDocument = createWritableInstance();

        var event = TestDataGenerator.generateSingleIntervalStartEvent().asList().get(0);
        writableDocument.appendEvent(event);

        testReadBack(writableDocument, document -> {
            List<Trace> traces = new ArrayList<>();
            document.streamTraces(new TraceFilters(), traces::add);

            assertEquals(1, traces.size());

            var trace = traces.get(0);

            assertInstanceOf(IntervalTrace.class, trace);

            var intervalTrace = (IntervalTrace) trace;
            assertEquals(event.getTimeNs(), intervalTrace.getStartTimeNs());
            assertEquals(TraceDocument.UNDEFINED_END_TIME_NS, intervalTrace.getEndTimeNs());
            assertEquals(event.getTimeNs(), document.getEarliestEventStartTimeNs());
            assertEquals(TraceDocument.UNDEFINED_END_TIME_NS, document.getLatestEventEndTimeNs());

            assertAttributeEquals(event, trace);
        });
    }
    
    @Test
    public void testAppendThenStream_intervalEndEvent_traceEventHasNoStartTime() throws Exception {
        var writableDocument = createWritableInstance();

        var event = TestDataGenerator.generateSingleIntervalEndEvent().asList().get(0);
        writableDocument.appendEvent(event);

        testReadBack(writableDocument, document -> {
            List<Trace> traces = new ArrayList<>();
            document.streamTraces(new TraceFilters(), traces::add);

            assertEquals(1, traces.size());

            var trace = traces.get(0);

            assertInstanceOf(IntervalTrace.class, trace);

            var intervalTrace = (IntervalTrace) trace;
            assertEquals(TraceDocument.UNDEFINED_START_TIME_NS, intervalTrace.getStartTimeNs());
            assertEquals(event.getTimeNs(), intervalTrace.getEndTimeNs());
            assertEquals(TraceDocument.UNDEFINED_START_TIME_NS, document.getEarliestEventStartTimeNs());
            assertEquals(event.getTimeNs(), document.getLatestEventEndTimeNs());

            assertAttributeEquals(event, trace);
        });
    }
    
    @Test
    public void testAppendThenStream_intervalStartAndEndEvent_treatedAsPair() throws Exception {
        var writableDocument = createWritableInstance();

        var durationNs = TimeUnit.MILLISECONDS.toNanos(400);
        var eventPair = TestDataGenerator.generateIntervalEventPair(durationNs).asList();

        var startEvent = eventPair.get(0);
        var endEvent = eventPair.get(1);

        writableDocument.appendEvent(startEvent);
        writableDocument.appendEvent(endEvent);

        testReadBack(writableDocument, document -> {
            List<Trace> traces = new ArrayList<>();
            document.streamTraces(new TraceFilters(), traces::add);

            assertEquals(1, traces.size());

            var trace = traces.get(0);

            assertInstanceOf(IntervalTrace.class, trace);

            var intervalTrace = (IntervalTrace) trace;
            assertEquals(startEvent.getTimeNs(), intervalTrace.getStartTimeNs());
            assertEquals(endEvent.getTimeNs(), intervalTrace.getEndTimeNs());
            assertEquals(startEvent.getTimeNs(), document.getEarliestEventStartTimeNs());
            assertEquals(endEvent.getTimeNs(), document.getLatestEventEndTimeNs());

            assertAttributeEquals(startEvent, trace);
            assertAttributeEquals(endEvent, trace);
        });
    }

    private void assertAttributeEquals(Event event, Trace trace) {
        assertEquals(event.getAttributeNames().size(), trace.getAttributes().size());

        for (String name : event.getAttributeNames()) {
            String eventValue = event.getAttribute(name);
            String traceValue = trace.getAttributes().get(name);

            assertEquals(eventValue, traceValue);
        }
    }

    private void testReadBack(WritableTraceDocument writableDocument, TestCode testCode) throws Exception {
        // First read from test document
        try {
            testCode.runWith(writableDocument);
        } finally {
            writableDocument.unload();
        }

        // Try read from a read-only instance (tests the alternative loading path)
        var readOnlyDocument = createInstance(writableDocument.getPath());

        try {
            testCode.runWith(readOnlyDocument);
        } finally {
            readOnlyDocument.unload();
        }
    }

    @FunctionalInterface
    private interface TestCode {
        void runWith(TraceDocument document) throws Exception;
    }
}
