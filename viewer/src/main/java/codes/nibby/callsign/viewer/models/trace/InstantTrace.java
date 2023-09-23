package codes.nibby.callsign.viewer.models.trace;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class InstantTrace extends Trace {

    private final long timeMs;

    public InstantTrace(Map<String, String> attributes, long timeMs) {
        super(attributes);
        this.timeMs = timeMs;
    }

    public long getTimeMs() {
        return timeMs;
    }

    @Override
    public Collection<Long> getNotableTimeInstances() {
        return List.of(timeMs);
    }
}
