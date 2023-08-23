package codes.nibby.callsign.viewer.misc;

public interface ProgressReporter extends CancellationToken {

    void notifyComplete();
    void notifyProgressChanged(double progress);
    void notifyProgressIndeterminate(boolean isIndeterminate);
    void notifyProgressMessageChanged(String message);

}
