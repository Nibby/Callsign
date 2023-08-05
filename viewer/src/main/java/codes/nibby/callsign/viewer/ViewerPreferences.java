package codes.nibby.callsign.viewer;

import java.nio.file.Path;

public interface ViewerPreferences {

    Path getImportDirectoryForTraceFiles();
    void setLastUsedImportDirectoryForTraceFiles(Path directory);

    Path getOpenDirectoryForTimelineDigestFile();
    void setLastUsedOpenDirectoryForDigestFile(Path directory);


}
