package codes.nibby.callsign.viewer.models;

import codes.nibby.callsign.api.Event;
import codes.nibby.callsign.viewer.TestDataGenerator;
import codes.nibby.callsign.viewer.TestHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public abstract class WritableTraceDocumentTest<TDocumentImpl extends WritableTraceDocument> {

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

    protected abstract TDocumentImpl createInstance(Path testDir);

    private TDocumentImpl createInstance() {
        return createInstance(this.testDir);
    }

    @Test
    public void testAppendThenStream_instantEvent_single() throws IOException, TraceDocumentAccessException {
        var document = createInstance();
        document.initialize();

        var event = TestDataGenerator.generateSingleInstantEvent().asList().get(0);
        document.appendEvent(event);

        List<Trace> traces = new ArrayList<>();
        document.streamTraces(List.of(), traces::add);

        assertEquals(1, traces.size());

        var trace = traces.get(0);

        assertInstanceOf(InstantTrace.class, trace);
        assertAttributeEquals(event, trace);
    }

    @Test
    public void testAppendThenStream_intervalStartEvent_traceEventHasNoEndTime() throws IOException, TraceDocumentAccessException {
        var document = createInstance();
        document.initialize();

        var event = TestDataGenerator.generateSingleIntervalStartEvent().asList().get(0);
        document.appendEvent(event);

        List<Trace> traces = new ArrayList<>();
        document.streamTraces(List.of(), traces::add);

        assertEquals(1, traces.size());

        var trace = traces.get(0);

        assertInstanceOf(IntervalTrace.class, trace);

        var intervalTrace = (IntervalTrace) trace;
        assertEquals(event.getTimeNs(), intervalTrace.getStartTimeNs());
        assertEquals(TraceDocument.UNDEFINED_END_TIME_NS, intervalTrace.getEndTimeNs());

        assertAttributeEquals(event, trace);
    }
    
    @Test
    public void testAppendThenStream_intervalEndEvent_traceEventHasNoStartTime() throws IOException, TraceDocumentAccessException {
        var document = createInstance();
        document.initialize();

        var event = TestDataGenerator.generateSingleIntervalEndEvent().asList().get(0);
        document.appendEvent(event);

        List<Trace> traces = new ArrayList<>();
        document.streamTraces(List.of(), traces::add);

        assertEquals(1, traces.size());

        var trace = traces.get(0);

        assertInstanceOf(IntervalTrace.class, trace);

        var intervalTrace = (IntervalTrace) trace;
        assertEquals(TraceDocument.UNDEFINED_START_TIME_NS, intervalTrace.getStartTimeNs());
        assertEquals(event.getTimeNs(), intervalTrace.getEndTimeNs());

        assertAttributeEquals(event, trace);
    }
    
    @Test
    public void testAppendThenStream_intervalStartAndEndEvent_treatedAsPair() throws IOException, TraceDocumentAccessException {
        var document = createInstance();
        document.initialize();

        var durationNs = TimeUnit.MILLISECONDS.toNanos(400);
        var eventPair = TestDataGenerator.generateIntervalEventPair(durationNs).asList();

        var startEvent = eventPair.get(0);
        var endEvent = eventPair.get(1);

        document.appendEvent(startEvent);
        document.appendEvent(endEvent);

        List<Trace> traces = new ArrayList<>();
        document.streamTraces(List.of(), traces::add);

        assertEquals(1, traces.size());

        var trace = traces.get(0);

        assertInstanceOf(IntervalTrace.class, trace);

        var intervalTrace = (IntervalTrace) trace;
        assertEquals(startEvent.getTimeNs(), intervalTrace.getStartTimeNs());
        assertEquals(endEvent.getTimeNs(), intervalTrace.getEndTimeNs());

        assertAttributeEquals(startEvent, trace);
        assertAttributeEquals(endEvent, trace);
    }


    private void assertAttributeEquals(Event event, Trace trace) {
        assertEquals(event.getAttributeNames().size(), trace.getAttributes().size());

        for (String name : event.getAttributeNames()) {
            String eventValue = event.getAttribute(name);
            String traceValue = trace.getAttributes().get(name);

            assertEquals(eventValue, traceValue);
        }
    }
}
