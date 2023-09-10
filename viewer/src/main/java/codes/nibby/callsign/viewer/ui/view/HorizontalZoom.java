package codes.nibby.callsign.viewer.ui.view;

import com.google.common.base.Preconditions;

/**
 * A primitive used in lieu of a {@code double} to represent horizontal zoom level in the trace viewer.
 * On top of enforcing invariants, the class provides several useful methods to handle calculations
 * between time represented by pixels and pixel quantities to represent time.
 */
public final class HorizontalZoom {

    // We define 100% zoom = 1d (zoom value) = 50 pixels to represent 1 second.
    // Rest of the program measures time in nanoseconds, but the concept is identical.
    private static final double NS_PER_PIXEL_AT_100_PERCENT_ZOOM = 20_000_000;


    public final double value;

    private HorizontalZoom(double value) {
        Preconditions.checkArgument(value > 0d, "Value must be > 0");

        this.value = value;
    }

    public double nsPerPixel() {
        return NS_PER_PIXEL_AT_100_PERCENT_ZOOM / value;
    }

    public double measurePixels(long timeDurationNs) {
        return Math.ceil(timeDurationNs / nsPerPixel());
    }

    public double measureTimeNs(double pixels) {
        return pixels * nsPerPixel();
    }

    public HorizontalZoom adjust(double increment) {
        if (value + increment <= 0) {
            return this;
        }

        return new HorizontalZoom(value + increment);
    }

    public static HorizontalZoom of(double value) {
        return new HorizontalZoom(value);
    }
}
