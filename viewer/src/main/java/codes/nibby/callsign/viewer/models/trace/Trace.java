package codes.nibby.callsign.viewer.models.trace;

import codes.nibby.callsign.api.Event;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public sealed abstract class Trace permits InstantTrace, IntervalTrace {

    private final String name;
    private final Map<String, String> attributes;

    @Nullable
    private TraceTrack track = null;

    public Trace(Map<String, String> attributes) {
        this.attributes = attributes;
        this.name = attributes.get(Event.SPECIAL_NAME_ATTRIBUTE);
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    @Nullable
    public TraceTrack getTrack() {
        return track;
    }

    public void setTrack(@Nullable TraceTrack track) {
        this.track = track;
    }

    public abstract Collection<Long> getNotableTimeInstances();

}
