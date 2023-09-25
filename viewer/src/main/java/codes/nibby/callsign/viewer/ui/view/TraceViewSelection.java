package codes.nibby.callsign.viewer.ui.view;

import codes.nibby.callsign.viewer.models.trace.Trace;

import java.util.Collection;
import java.util.Optional;

interface TraceViewSelection {

    Collection<Trace> getSelectedTraces();

    Optional<Trace> getHoveredTrace();

}
