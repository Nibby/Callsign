package codes.nibby.callsign.viewer.ui.view;

import codes.nibby.callsign.viewer.models.trace.Trace;
import codes.nibby.callsign.viewer.models.trace.TraceTrack;

import java.util.*;

final class TraceContent {

    private final String trackDisplayAttributeName;
    private final String trackBinningAttributeName;

    private final Map<TraceTrack, TrackData> trackData = new LinkedHashMap<>();
    private final Map<String, TraceTrack> tracks = new HashMap<>();

    private DisplayData displayData = null;

    private Long earliestTraceEventStartMs = null;
    private Long latestTraceEventEndMs = null;

    TraceContent(String trackDisplayAttributeName, String trackBinningAttributeName) {
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

        earliestTraceEventStartMs = (earliestTraceEventStartMs == null)
            ? track.getEarliestEntryTimeMs()
            : Math.min(earliestTraceEventStartMs, track.getEarliestEntryTimeMs());

        latestTraceEventEndMs = (latestTraceEventEndMs == null)
            ? track.getLatestEntryEndTimeMs()
            : Math.max(latestTraceEventEndMs, track.getLatestEntryEndTimeMs());

        displayData = null;
    }

    void computeDisplayData() {
        if (displayData == null) {
            displayData = new DisplayData();
            displayData.trackDisplayOrder.addAll(trackData.keySet());
        }

        int cumulativeBandIndex = 0;
        int displayIndex = 0;

        for (TraceTrack track : displayData.trackDisplayOrder) {
            TrackData trackData = this.trackData.get(track);

            final int cumulativeBandIndexStart = cumulativeBandIndex;
            final int cumulativeBandIndexEnd = cumulativeBandIndexStart + trackData.getBandCount();

            for (int band = 0; band < trackData.getBandCount(); band++, cumulativeBandIndex++) {
                var trackDisplayData = new DisplayData.TrackDisplayData(
                    cumulativeBandIndexStart,
                    cumulativeBandIndexEnd,
                    track,
                    displayIndex
                );

                displayData.trackLookupByCumulativeBandIndex.put(cumulativeBandIndex, trackDisplayData);
            }

            displayIndex++;
        }
    }

    public DisplayData getDisplayData() {
        return Objects.requireNonNull(displayData);
    }

    public Long getEarliestTraceEventStartMs() {
        return earliestTraceEventStartMs;
    }

    public Long getLatestTraceEventEndMs() {
        return latestTraceEventEndMs;
    }

    public static final class DisplayData {

        private final List<TraceTrack> trackDisplayOrder = new ArrayList<>();
        private final Map<Integer, TrackDisplayData> trackLookupByCumulativeBandIndex = new TreeMap<>();

        public record TrackDisplayData(
            int cumulativeBandDisplayIndexStart,
            int cumulativeBandDisplayIndexEnd,
            TraceTrack track,
            int trackDisplayIndex
        ) {}

        public Optional<TrackDisplayData> getTrackDataFromCumulativeBandIndex(int cumulativeBandIndex) {
            return Optional.ofNullable(trackLookupByCumulativeBandIndex.get(cumulativeBandIndex));
        }

        public int getTotalBands() {
            return trackLookupByCumulativeBandIndex.size();
        }
    }
}
