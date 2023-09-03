package codes.nibby.callsign.viewer;

import codes.nibby.callsign.viewer.importer.TraceDocumentCreateWizard;
import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

public final class ViewerApplication extends Application {

    private final ViewerPreferences preferences;
    private final ViewerApplicationController controller;

    private Stage primaryStage;

    public ViewerApplication() {
        this.preferences = new ViewerPreferencesImpl();
        this.controller = new ViewerApplicationControllerImpl(this);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;

        var pane = new FlowPane(Orientation.VERTICAL);
        {
            var importTrace = new Button("New Trace Digest");
            importTrace.setOnAction(event -> createDigest(primaryStage));

            var openTimelineDigest = new Button("Open Trace Digest");
            openTimelineDigest.setOnAction(event -> openDigest());

            pane.getChildren().addAll(importTrace, openTimelineDigest);
        }

        var scene = new Scene(pane);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void createDigest(Stage stage) {
        TraceDocumentCreateWizard.begin(controller, stage, preferences);
    }

    private void openDigest() {

    }

    public static void main(String[] args) {
        launch(args);
    }

    public void hideLandingScreen() {
        this.primaryStage.hide();
    }

    public void showLandingScreen() {
        this.primaryStage.show();
    }
}
