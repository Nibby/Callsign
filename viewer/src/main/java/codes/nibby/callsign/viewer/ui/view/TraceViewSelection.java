package codes.nibby.callsign.viewer.ui.view;

import codes.nibby.callsign.viewer.models.trace.Trace;

import java.util.Collection;

interface TraceViewSelection {

    Collection<Trace> getSelectedTraces();

    Collection<Trace> getHoveredTraces();

}
