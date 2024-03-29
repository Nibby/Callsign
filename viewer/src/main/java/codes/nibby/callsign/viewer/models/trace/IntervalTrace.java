package codes.nibby.callsign.viewer.models.trace;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class IntervalTrace extends Trace {

    private final long startTimeMs;
    private final long endTimeMs;

    public IntervalTrace(
        @Nullable Map<String, String> attributesAtStart,
        @Nullable Map<String, String> attributesAtEnd,
        long startTimeMs,
        long endTimeMs
    ) {
        super(coalesceAttributes(attributesAtStart, attributesAtEnd));

        this.startTimeMs = startTimeMs;
        this.endTimeMs = endTimeMs;
    }

    private static Map<String, String> coalesceAttributes(
        @Nullable Map<String, String> attributesAtStart,
        @Nullable Map<String, String> attributesAtEnd
    ) {
        if (attributesAtStart == null && attributesAtEnd == null) {
            throw new IllegalStateException("Should not create TraceEvent if missing both start and end interval events");
        }
        if (attributesAtStart == null) {
            return attributesAtEnd;
        }
        if (attributesAtEnd == null) {
            return attributesAtStart;
        }

        // TODO: Can do something fancy here if attributes at start != attributes at end,
        //       for example showing how the attributes changed during the lifecycle of the event
        //       To keep things simple for now, just return the final attributes

        return attributesAtEnd;
    }

    public long getStartTimeMs() {
        return startTimeMs;
    }

    public long getEndTimeMs() {
        return endTimeMs;
    }

    @Override
    public Collection<Long> getNotableTimeInstances() {
        return List.of(startTimeMs, endTimeMs);
    }
}
