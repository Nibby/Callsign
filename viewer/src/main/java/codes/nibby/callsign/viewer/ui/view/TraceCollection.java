package codes.nibby.callsign.viewer.ui.view;

import codes.nibby.callsign.viewer.models.trace.Trace;
import codes.nibby.callsign.viewer.models.trace.TraceTrack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

final class TraceCollection {

    private final String trackDisplayAttributeName;
    private final String trackBinningAttributeName;

    private final Map<TraceTrack, TrackData> trackData = new TreeMap<>();
    private final Map<String, TraceTrack> tracks = new HashMap<>();

    private Long earliestTraceEventStartNs = null;
    private Long latestTraceEventEndNs = null;

    TraceCollection(String trackDisplayAttributeName, String trackBinningAttributeName) {
        this.trackDisplayAttributeName = trackDisplayAttributeName;
        this.trackBinningAttributeName = trackBinningAttributeName;
    }

    public Map<TraceTrack, TrackData> getTrackData() {
        return Collections.unmodifiableMap(trackData);
    }

    void addTrace(Trace trace) {
        Map<String, String> attributes = trace.getAttributes();
        String binningAttributeValue = attributes.get(trackBinningAttributeName);

        TraceTrack track = tracks.computeIfAbsent(
            binningAttributeValue,
            binningValue -> new TraceTrack(trackBinningAttributeName, binningValue, trackDisplayAttributeName)
        );

        trackData.computeIfAbsent(track, key -> new TrackData()).addTrace(trace);

        trace.setTrack(track);
        track.notifyTraceAdded(trace);

        earliestTraceEventStartNs = (earliestTraceEventStartNs == null)
            ? track.getEarliestEntryTimeNs()
            : Math.min(earliestTraceEventStartNs, track.getEarliestEntryTimeNs());

        latestTraceEventEndNs = (latestTraceEventEndNs == null)
            ? track.getLatestEntryEndTimeNs()
            : Math.max(latestTraceEventEndNs, track.getLatestEntryEndTimeNs());
    }
}
