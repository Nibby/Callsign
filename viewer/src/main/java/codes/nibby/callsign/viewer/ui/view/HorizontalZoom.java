package codes.nibby.callsign.viewer.ui.view;

import com.google.common.base.Preconditions;

/**
 * A primitive used in lieu of a {@code double} to represent horizontal zoom level in the trace viewer.
 * On top of enforcing invariants, the class provides several useful methods to handle calculations
 * between time represented by pixels and pixel quantities to represent time.
 */
public final class HorizontalZoom {

    // We define 100% zoom = 1d (zoom value) = 50 pixels to represent 1 second.
    // Rest of the program measures time in milliseconds, but the concept is identical.
    private static final double MS_PER_PIXEL_AT_100_PERCENT_ZOOM = 20;

    public final double value;

    private HorizontalZoom(double value) {
        Preconditions.checkArgument(value > 0d, "Value must be > 0");

        this.value = value;
    }

    /**
     * @return Number of milliseconds represented by each pixel on screen.
     */
    public double msPerPixel() {
        return MS_PER_PIXEL_AT_100_PERCENT_ZOOM / value;
    }

    /**
     * Converts a time interval to pixel width under this zoom factor.
     *
     * @param timeDurationMs Time (in milliseconds) to measure
     * @return Number of pixels to represent the given time
     */
    public double measurePixels(long timeDurationMs) {
        return Math.ceil(timeDurationMs / msPerPixel());
    }

    /**
     * Converts pixels to a time interval (milliseconds) under this zoom factor.
     *
     * @param pixels Pixels to convert to time
     * @return Time duration represented by the pixels, in milliseconds
     */
    public double measureTimeMs(double pixels) {
        return pixels * msPerPixel();
    }

    /**
     * Apply an increment scale factor on top of the current, returning a new horizontal zoom
     * instance.
     *
     * @param increment Zoom factor increment.
     * @return Adjusted horizontal zoom factor (new instance)
     */
    public HorizontalZoom adjust(double increment) {
        if (value + increment <= 0) {
            return this;
        }

        return new HorizontalZoom(value + increment);
    }

    /**
     * Creates an instance of HorizontalZoom from a give scale factor.
     *
     * @param scaleFactor Scale factor.
     * @return HorizontalZoom instance representing this scale factor.
     */
    public static HorizontalZoom of(double scaleFactor) {
        Preconditions.checkArgument(scaleFactor > 0d, "Scale factor must be > 0d");

        return new HorizontalZoom(scaleFactor);
    }
}
