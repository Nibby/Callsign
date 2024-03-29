package codes.nibby.callsign.viewer.ui;

import codes.nibby.callsign.viewer.misc.ProgressReporter;
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

        var contentPane = new FlowPane(Orientation.VERTICAL);
        contentPane.getChildren().addAll(messageTitleLabel, messageLabel, progressBar);

        var dialogContent = new DialogPane();
        dialogContent.setContent(contentPane);
        dialogContent.getButtonTypes().add(new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE));

        dialog = new Dialog<>();
        dialog.setDialogPane(dialogContent);
        dialog.setOnCloseRequest(event -> cancel());
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

    public void cancel() {
        canceled = true;
    }

    @Override
    public boolean isCancelRequested() {
        return canceled;
    }
}
