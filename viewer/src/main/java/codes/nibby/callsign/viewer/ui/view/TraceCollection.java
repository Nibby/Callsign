package codes.nibby.callsign.viewer.ui.view;

import codes.nibby.callsign.viewer.models.TraceDocument;
import codes.nibby.callsign.viewer.models.TraceDocumentAccessException;

import java.util.*;

final class TraceCollection {

    private final Map<Track, TrackData> trackData = new TreeMap<>();
    private final Map<String, Track> tracks = new HashMap<>();

    private Long earliestTraceEventStartNs = null;
    private Long latestTraceEventEndNs = null;

    public void compute(String trackAttributeName, TraceDocument document) {
        trackData.clear();
        tracks.clear();

        try {
            document.streamEntries(List.of(), event -> {
                Map<String, String> attributes = event.getAttributes();
                String trackAttributeValue = attributes.get(trackAttributeName);

                Track track = tracks.computeIfAbsent(trackAttributeValue, Track::new);

                trackData.computeIfAbsent(track, key -> new TrackData()).addTrace(event);
                track.notifyTraceAdded(event);

                earliestTraceEventStartNs = (earliestTraceEventStartNs == null)
                    ? track.getEarliestEntryTimeNs()
                    : Math.min(earliestTraceEventStartNs, track.getEarliestEntryTimeNs());

                latestTraceEventEndNs = (latestTraceEventEndNs == null)
                    ? track.getLatestEntryEndTimeNs()
                    : Math.max(latestTraceEventEndNs, track.getLatestEntryEndTimeNs());
            });
        } catch (TraceDocumentAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<Track, TrackData> getTrackData() {
        return Collections.unmodifiableMap(trackData);
    }

    public Optional<Long> getEarliestTraceEventStartNs() {
        return Optional.ofNullable(earliestTraceEventStartNs);
    }

    public Optional<Long> getLatestTraceEventEndNs() {
        return Optional.ofNullable(latestTraceEventEndNs);
    }
}
