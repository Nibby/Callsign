package codes.nibby.callsign.viewer.ui;

import javafx.application.Platform;

public final class UIHelper {

    private UIHelper() {

    }

    public static void runOnFxApplicationThread(Runnable runnable) {
        if (isOnFxApplicationThread()) {
            runnable.run();
        } else {
            Platform.runLater(runnable);
        }
    }

    public static void requireFxApplicationThread() {
        if (!isOnFxApplicationThread()) {
            throw new IllegalStateException("This code must be run on FX application thread");
        }
    }

    public static boolean isOnFxApplicationThread() {
        return Platform.isFxApplicationThread();
    }
}
