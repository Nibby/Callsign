package codes.nibby.callsign.viewer.importer;

import codes.nibby.callsign.viewer.ViewerApplicationController;
import codes.nibby.callsign.viewer.ViewerPreferences;
import codes.nibby.callsign.viewer.models.TraceDocument;
import codes.nibby.callsign.viewer.models.TraceDocumentFormat;
import codes.nibby.callsign.viewer.ui.ProgressDialog;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static codes.nibby.callsign.viewer.importer.TraceDocumentAssembler.AssemblyOptions;

public final class TraceDocumentCreateWizard {

    public static void begin(ViewerApplicationController controller, Stage stage, ViewerPreferences preferences) {
        Optional<List<RawTraceFile>> initialFileSelection = promptFileSelection(stage, preferences);

        if (initialFileSelection.isEmpty()) {
            return;
        }

        Optional<AssemblyOptions> importOptions = confirmImportOptions(initialFileSelection.get(), preferences);

        if (importOptions.isEmpty()) {
            return;
        }

        beginDigestAssembly(controller, importOptions.get());
    }

    private static Optional<List<RawTraceFile>> promptFileSelection(Stage stage, ViewerPreferences preferences) {
        var importDirectory = preferences.getImportDirectoryForTraceFiles().toFile();

        List<String> supportedExtensions = RawTraceFile.getSupportedFileExtensions();
        List<String> supportedExtensionsRegexFormat = supportedExtensions.stream()
            .map(extension -> "*." + extension)
            .collect(Collectors.toList());

        var fileChooser = new FileChooser();
        fileChooser.setTitle("Select raw trace file(s) to import");
        fileChooser.setInitialDirectory(importDirectory);
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Callsign Raw Trace File", supportedExtensionsRegexFormat));

        List<File> files = fileChooser.showOpenMultipleDialog(stage);

        if (files == null) {
            return Optional.empty();
        }

        List<RawTraceFile> inputTraceFiles = filterAndWrapSelectionAsInputTraceFile(files, supportedExtensions);

        if (!inputTraceFiles.isEmpty()) {
            Path selectedFolder = files.get(0).toPath().getParent();
            preferences.setLastUsedOpenDirectoryForDigestFile(selectedFolder);
        }

        // Files may be empty, but at least user has followed through with the open dialog so proceed to next step
        return Optional.of(inputTraceFiles);
    }

    private static Optional<AssemblyOptions> confirmImportOptions(List<RawTraceFile> initialFileSelection, ViewerPreferences preferences) {
        // Code is temporary (proof-of-concept)
        // TODO: UI
        //       - Summarise files that will be imported
        //       - Allow removing files that should not be imported
        //       - Allow adding additional files to be imported (do not count same files twice)
        //       - Allow choosing output digest file path
        //       - Check output digest file already exists and warn about overwrite

        var filesToImport = initialFileSelection;

        if (filesToImport.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Nothing to import");
            alert.show();
            return Optional.empty();
        }

        var outputFile = Paths.get(System.getProperty("user.dir")).resolve("digest.cstd");

        if (Files.exists(outputFile)) {
            try {
                Files.delete(outputFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return Optional.of(new AssemblyOptions(filesToImport, outputFile));
    }

    private static List<RawTraceFile> filterAndWrapSelectionAsInputTraceFile(List<File> selection, List<String> supportedExtensions) {
        return selection.stream()
            .filter(file -> isSupportedExtension(file, supportedExtensions))
            .map(file -> new RawTraceFile(file.toPath()))
            .collect(Collectors.toList());
    }

    private static void beginDigestAssembly(ViewerApplicationController controller, AssemblyOptions assemblyOptions) {
        var progressDialog = new ProgressDialog("Assembling Timeline Digest");
        progressDialog.show();

        var assemblyTaskThread = new Thread(() -> assembleAndOpenViewerAsync(controller, assemblyOptions, progressDialog));
        assemblyTaskThread.start();
    }

    private static void assembleAndOpenViewerAsync(ViewerApplicationController controller, AssemblyOptions assemblyOptions, ProgressDialog progressDialog) {
        var assembler = TraceDocumentFormat.SQLITE.createAssembler();

        TraceDocument assembledDocument;
        try {
            assembledDocument = assembler.assemble(assemblyOptions, progressDialog);
        } catch (IOException e) {
            progressDialog.notifyComplete();

            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("An error occurred while importing trace data");
                alert.setContentText(e.toString());

                alert.show();
            });

            throw new RuntimeException(e);
        }

        controller.openViewer(assembledDocument);
    }

    private static boolean isSupportedExtension(File file, List<String> supportedExtensions) {
        for (String extension : supportedExtensions) {
            if (file.getName().endsWith(extension)) {
                return true;
            }
        }

        return false;
    }
}
