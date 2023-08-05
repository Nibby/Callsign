package codes.nibby.callsign.viewer;

import java.nio.file.Path;
import java.nio.file.Paths;

class ViewerPreferencesImpl implements ViewerPreferences {

    private Path importDirectoryForTrace = Paths.get(System.getProperty("user.dir"));
    private Path openDirectoryForDigest = Paths.get(System.getProperty("user.dir"));

    @Override
    public Path getImportDirectoryForTraceFiles() {
        return importDirectoryForTrace;
    }

    @Override
    public void setLastUsedImportDirectoryForTraceFiles(Path directory) {
        this.importDirectoryForTrace = directory;
    }

    @Override
    public Path getOpenDirectoryForTimelineDigestFile() {
        return openDirectoryForDigest;
    }

    @Override
    public void setLastUsedOpenDirectoryForDigestFile(Path directory) {
        this.openDirectoryForDigest = directory;
    }
}
