package codes.nibby.callsign.viewer.models.trace;

import codes.nibby.callsign.api.Event;

import java.util.Collections;
import java.util.Map;

public abstract class Trace {

    private final String name;
    private final Map<String, String> attributes;

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
}
