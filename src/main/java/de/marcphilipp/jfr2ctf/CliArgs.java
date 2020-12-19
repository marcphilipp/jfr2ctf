package de.marcphilipp.jfr2ctf;

import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.List;

import static org.apache.commons.io.FilenameUtils.removeExtension;

@Command(name = "jfr2ctf")
class CliArgs {

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    boolean help;

    @Nullable
    @Option(names = {"--include-events"}, description = "events to include (defaults to all)")
    List<String> eventTypes;

    @Parameters(arity = "1", index = "0", paramLabel = "JFR_FILE", description = "JFR file to convert")
    Path jfrFile;

    @Nullable
    @Parameters(arity = "0..1", index = "1", paramLabel = "CTF_FILE", description = "Result file to write (defaults to JFR_FILE with json extension)")
    Path ctfFile;

    Path resolveCtfFile() {
        return ctfFile == null
                ? jfrFile.resolveSibling(removeExtension(jfrFile.getFileName().toString()) + ".json")
                : ctfFile;
    }
}
