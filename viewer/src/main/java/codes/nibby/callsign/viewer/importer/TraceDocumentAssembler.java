package codes.nibby.callsign.viewer.importer;

import codes.nibby.callsign.viewer.misc.ProgressReporter;
import codes.nibby.callsign.viewer.models.document.TraceDocument;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface TraceDocumentAssembler {

    TraceDocument assemble(AssemblyOptions options, ProgressReporter progressReporter) throws IOException;

    final class AssemblyOptions {

        public final List<RawTraceFile> inputTraceFiles;
        public final Path outputFile;

        public AssemblyOptions(List<RawTraceFile> inputTraceFiles, Path outputFile) {
            this.inputTraceFiles = inputTraceFiles;
            this.outputFile = outputFile;
        }

    }
}
