package codes.nibby.callsign.viewer.ui.view;

import codes.nibby.callsign.viewer.models.trace.Trace;
import com.google.common.base.Preconditions;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;

final class TraceViewSelectionManager implements TraceViewSelection {

    @Nullable
    private Trace hoveredTrace = null;

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

    public void clearHoveredTrace() {
        hoveredTrace = null;
    }

    public void setHoveredTrace(@Nullable Trace trace) {
        hoveredTrace = trace;
    }

    @Override
    public Optional<Trace> getHoveredTrace() {
        return Optional.ofNullable(hoveredTrace);
    }

    public void clearAll() {
        clearSelectedTraces();
        clearHoveredTrace();
    }
}
