package codes.nibby.callsign.viewer.ui.view;

final class TraceViewContentManager {

    // Size of the canvas
    private double viewportWidth;
    private double viewportHeight;

    private double contentWidth;
    private double contentHeight;

    private double trackHeight = 40;
    private double trackZoom = 1d;

    private static final double EPSILON = 0.001d;

    public TraceViewContentManager() {

    }

    public void computeParametersForViewport(double width, double height, long timelineStartTimeNs, long timelineEndTimeNs) {
        updateViewportSize(width, height);

    }

    private void updateViewportSize(double width, double height) {
        if (Math.abs(viewportWidth - width) > EPSILON) {
            viewportWidth = width;
        }

        if (Math.abs(viewportHeight - height) > EPSILON) {
            viewportHeight = height;
        }
    }
}
