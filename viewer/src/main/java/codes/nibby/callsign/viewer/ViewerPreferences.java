package codes.nibby.callsign.viewer;

import java.nio.file.Path;

/**
 * Values persisted to preference store between app sessions.
 */
public interface ViewerPreferences {

    /**
     * @return Last used directory for importing raw trace files.
     */
    Path getImportDirectoryForTraceFiles();

    /**
     * Sets last used directory for importing raw trace files.
     *
     * @param directory Last used import directory.
     */
    void setLastUsedImportDirectoryForTraceFiles(Path directory);

    /**
     * @return Last used directory for opening a timeline digest file.
     */
    Path getOpenDirectoryForTimelineDigestFile();

    /**
     * Sets last used directory for opening timeline digest files.
     *
     * @param directory Last used directory.
     */
    void setLastUsedOpenDirectoryForDigestFile(Path directory);

}
