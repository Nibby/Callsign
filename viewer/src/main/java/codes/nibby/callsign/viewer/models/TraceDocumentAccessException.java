package codes.nibby.callsign.viewer.models;

public final class TraceDocumentAccessException extends Exception {

    public TraceDocumentAccessException() {
        super();
    }

    public TraceDocumentAccessException(String message) {
        super(message);
    }

    public TraceDocumentAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public TraceDocumentAccessException(Throwable cause) {
        super(cause);
    }

    protected TraceDocumentAccessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
