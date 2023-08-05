package codes.nibby.callsign.viewer;

import codes.nibby.callsign.viewer.importer.TimelineDigestDocumentCreateWizard;
import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

public final class ViewerApplication extends Application {

    private final ViewerPreferences preferences;
    private final ViewerApplicationController controller;

    public ViewerApplication() {
        this.preferences = new ViewerPreferencesImpl();
        this.controller = new ViewerApplicationControllerImpl();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        var pane = new FlowPane(Orientation.VERTICAL);
        {
            var importTrace = new Button("Create a new timeline digest");
            importTrace.setOnAction(event -> createDigest(primaryStage));

            var openTimelineDigest = new Button("Open an existing timeline digest");
            openTimelineDigest.setOnAction(event -> openDigest());

            pane.getChildren().addAll(importTrace, openTimelineDigest);
        }

        var scene = new Scene(pane);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void createDigest(Stage stage) {
        TimelineDigestDocumentCreateWizard.begin(controller, stage, preferences);
    }

    private void openDigest() {

    }

    public static void main(String[] args) {
        launch(args);
    }
}
