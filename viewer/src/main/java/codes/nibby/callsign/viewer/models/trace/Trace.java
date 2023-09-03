package codes.nibby.callsign.viewer.models.trace;

import codes.nibby.callsign.api.Event;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;

public abstract class Trace {

    private final String name;
    private final Map<String, String> attributes;

    @Nullable
    private TraceTrack track = null;

    private boolean needsProcessing = true;

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

         if (needsProcessing && track != null) {
             track.setNeedsProcessing(true);
         }
    }

    public boolean isNeedsProcessing() {
        return needsProcessing;
    }

    public void setNeedsProcessing(boolean needsProcessing) {
        this.needsProcessing = needsProcessing;

        if (needsProcessing && track != null) {
            track.setNeedsProcessing(true);
        }
    }
}
