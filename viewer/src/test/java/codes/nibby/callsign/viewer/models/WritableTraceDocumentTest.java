package codes.nibby.callsign.viewer.models;

import codes.nibby.callsign.api.Event;
import codes.nibby.callsign.viewer.TestDataGenerator;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class WritableTraceDocumentTest<TDocumentImpl extends WritableTraceDocument> {

    protected abstract TDocumentImpl createInstance();

    @Test
    public void testAppendThenStream_instantEvent_single() throws IOException, TraceDocumentAccessException {
        var document = createInstance();
        document.initialize();

        var event = TestDataGenerator.generateSingleInstantEvent().asList().get(0);
        document.appendEvent(event);

        List<TraceEvent> traces = new ArrayList<>();
        document.streamEntries(List.of(), traces::add);

        assertEquals(1, traces.size());

        var trace = traces.get(0);

        assertInstanceOf(InstantTrace.class, trace);
        assertAttributeEquals(event, trace);
    }

    @Test
    public void testAppendThenStream_intervalStartEvent_traceEventHasNoEndTime() throws IOException, TraceDocumentAccessException {
        // FIXME
        var document = createInstance();
        document.initialize();

        var event = TestDataGenerator.generateSingleIntervalStartEvent().asList().get(0);
        document.appendEvent(event);

        List<TraceEvent> traces = new ArrayList<>();
        document.streamEntries(List.of(), traces::add);

        assertEquals(1, traces.size());

        var trace = traces.get(0);

        assertInstanceOf(IntervalTrace.class, trace);

        var intervalTrace = (IntervalTrace) trace;
        assertEquals(event.getTimeNs(), intervalTrace.getStartTimeNs());
        assertEquals(TraceDocument.UNDEFINED_END_TIME_NS, intervalTrace.getEndTimeNs());

        assertAttributeEquals(event, trace);
    }
    
    @Test
    public void testAppendThenStream_intervalEndEvent_traceEventHasNoStartTime() {
        // TODO
    }
    
    @Test
    public void testAppendThenStream_intervalStartAndEndEvent_treatedAsPair() {
        // TODO
    }


    private void assertAttributeEquals(Event event, TraceEvent trace) {
        assertEquals(event.getAttributeNames().size(), trace.getAttributes().size());

        for (String name : event.getAttributeNames()) {
            String eventValue = event.getAttribute(name);
            String traceValue = trace.getAttributes().get(name);

            assertEquals(eventValue, traceValue);
        }
    }
}
