package codes.nibby.callsign.viewer.ui.view;

import codes.nibby.callsign.viewer.models.filters.TraceFilters;

import javax.annotation.Nullable;

public final class TraceViewDisplayOptions {

    @Nullable
    private String binningAttributeName = null;
    private boolean showIntervalTraces = true;
    private boolean showInstantTraces = true;
    private TraceViewColorScheme colorScheme = new TraceViewLightColorScheme();
    private TraceFilters filters = new TraceFilters();


    public TraceViewDisplayOptions() {

    }

    @Nullable
    public String getBinningAttributeName() {
        return binningAttributeName;
    }

    public void setBinningAttributeName(@Nullable String binningAttributeName) {
        this.binningAttributeName = binningAttributeName;
    }

    public boolean isShowIntervalTraces() {
        return showIntervalTraces;
    }

    public void setShowIntervalTraces(boolean showIntervalTraces) {
        this.showIntervalTraces = showIntervalTraces;
    }

    public boolean isShowInstantTraces() {
        return showInstantTraces;
    }

    public void setShowInstantTraces(boolean showInstantTraces) {
        this.showInstantTraces = showInstantTraces;
    }

    public TraceViewColorScheme getColorScheme() {
        return colorScheme;
    }

    public void setColorScheme(TraceViewColorScheme colorScheme) {
        this.colorScheme = colorScheme;
    }

    public TraceFilters getFilters() {
        return filters;
    }

    public void setFilters(TraceFilters filters) {
        this.filters = filters;
    }
}
