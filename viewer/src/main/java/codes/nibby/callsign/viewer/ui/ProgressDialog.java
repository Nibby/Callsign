package codes.nibby.callsign.viewer.ui;

import codes.nibby.callsign.viewer.ProgressReporter;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import org.jetbrains.annotations.Nullable;

public final class ProgressDialog implements ProgressReporter {

    private volatile boolean canceled = false;

    private final Dialog<Void> dialog;
    private final Label messageTitleLabel;
    private final Label messageLabel;
    private final ProgressBar progressBar;
    private final Button cancelButton;

    private double progressValue;

    public ProgressDialog() {
        this(null);
    }

    public ProgressDialog(@Nullable String messageTitle) {

        progressValue = 0d;

        messageTitleLabel = new Label();
        messageTitleLabel.setVisible(messageTitle != null);

        if (messageTitle != null) {
            messageTitleLabel.setText(messageTitle);
        }

        messageLabel = new Label();
        progressBar = new ProgressBar(progressValue);
        cancelButton = new Button("Cancel");

        var contentPane = new FlowPane(Orientation.VERTICAL);
        contentPane.getChildren().addAll(messageTitleLabel, messageLabel, progressBar, cancelButton);

        var dialogContent = new DialogPane();
        dialogContent.setContent(contentPane);

        dialog = new Dialog<>();
        dialog.setDialogPane(dialogContent);
    }

    public void show() {
        dialog.show();
    }

    public void showAndWait() {
        dialog.showAndWait();
    }

    @Override
    public void notifyComplete() {
        UIHelper.runOnFxApplicationThread(this::handleCompletion);
    }

    private void handleCompletion() {
        dialog.close();
    }

    @Override
    public void notifyProgressChanged(double progress) {
        UIHelper.runOnFxApplicationThread(() -> handleProgressChanged(progress));
    }

    private void handleProgressChanged(double progress) {
        progressValue = progress;
        progressBar.setProgress(progress);
    }

    @Override
    public void notifyProgressIndeterminate(boolean isIndeterminate) {
        UIHelper.runOnFxApplicationThread(() -> handleProgressIndeterminate(isIndeterminate));
    }

    private void handleProgressIndeterminate(boolean isIndeterminate) {
        progressBar.setProgress(isIndeterminate ? ProgressIndicator.INDETERMINATE_PROGRESS : progressValue);
    }

    @Override
    public void notifyProgressMessageChanged(String message) {
        UIHelper.runOnFxApplicationThread(() -> handleProgressMessageChanged(message));
    }

    private void handleProgressMessageChanged(String message) {
        messageLabel.setVisible(message != null);
        messageLabel.setText(message == null ? "" : message);
    }

    @Override
    public void requestCancellation() {
        canceled = true;
    }

    @Override
    public boolean isCancellationRequested() {
        return canceled;
    }
}
