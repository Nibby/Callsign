package codes.nibby.callsign.viewer.ui.view;

import codes.nibby.callsign.viewer.models.trace.Trace;
import com.google.common.base.Preconditions;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

final class TraceViewSelectionManager implements TraceViewSelection {

    private final Collection<Trace> hoveredTraces = new LinkedHashSet<>();
    private final Collection<Trace> selectedTraces = new LinkedHashSet<>();

    public void clearSelectedTraces() {
        selectedTraces.clear();
    }

    public void addSelectedTrace(Trace trace) {
        Preconditions.checkNotNull(trace);
        selectedTraces.add(trace);
    }

    public void removeSelectedTrace(Trace trace) {
        Preconditions.checkNotNull(trace);
        selectedTraces.remove(trace);
    }

    public void setSelectedTraces(Collection<Trace> traces) {
        Preconditions.checkNotNull(traces);
        selectedTraces.clear();
        selectedTraces.addAll(traces);
    }

    public void toggleSelectedTrace(Trace trace) {
        Preconditions.checkNotNull(trace);

        if (selectedTraces.contains(trace)) {
            removeSelectedTrace(trace);
        } else {
            addSelectedTrace(trace);
        }
    }

    @Override
    public Collection<Trace> getSelectedTraces() {
        return Collections.unmodifiableCollection(selectedTraces);
    }

    public void clearHoveredTraces() {
        hoveredTraces.clear();
    }

    public void setHoveredTraces(Collection<Trace> traces) {
        hoveredTraces.clear();
        hoveredTraces.addAll(traces);
    }

    @Override
    public Collection<Trace> getHoveredTraces() {
        return Collections.unmodifiableCollection(hoveredTraces);
    }

    public void clearAll() {
        clearSelectedTraces();
        clearHoveredTraces();
    }
}
