package codes.nibby.callsign.viewer;

public interface ProgressReporter {

    void notifyComplete();
    void notifyProgressChanged(double progress);
    void notifyProgressIndeterminate(boolean isIndeterminate);
    void notifyProgressMessageChanged(String message);

    void requestCancellation();
    boolean isCancellationRequested();

}
