package codes.nibby.callsign.viewer.misc;

import java.util.concurrent.atomic.AtomicBoolean;

public final class CancellationTokenSource implements CancellationToken {

    private final AtomicBoolean cancelRequested = new AtomicBoolean(false);

    public void cancel() {
        cancelRequested.set(true);
    }

    @Override
    public boolean isCancelRequested() {
        return cancelRequested.get();
    }
}
