package codes.nibby.callsign.viewer.models;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class TraceEntry {

    private final String name;
    private final Map<String, String> attributes;

    public TraceEntry(String name, Map<String, String> attributes) {
        this.name = name;
        this.attributes = attributes;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }
}
