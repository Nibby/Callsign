package codes.nibby.callsign.viewer.importer;

import codes.nibby.callsign.viewer.ProgressReporter;
import codes.nibby.callsign.viewer.models.TimelineDigestDocument;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface TimelineDigestDocumentAssembler {

    TimelineDigestDocument assemble(AssemblyOptions options, ProgressReporter progressReporter) throws IOException;

    final class AssemblyOptions {

        public final List<InputTraceFile> inputTraceFiles;
        public final Path outputFile;

        public AssemblyOptions(List<InputTraceFile> inputTraceFiles, Path outputFile) {
            this.inputTraceFiles = inputTraceFiles;
            this.outputFile = outputFile;
        }

    }
}
