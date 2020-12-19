package de.marcphilipp.jfr2ctf;

import org.jetbrains.annotations.Nullable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.PicocliException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

import static org.apache.commons.io.FilenameUtils.removeExtension;

public class Cli {

    public static void main(String[] rawArgs) throws Exception {
        Args args = new Args();
        try {
            new CommandLine(args).parseArgs(rawArgs);
            if (args.help) {
                CommandLine.usage(args, System.out);
            } else {
                run(args);
            }
        } catch (PicocliException e) {
            System.err.println(e.getMessage());
            System.err.println();
            CommandLine.usage(args, System.err);
            System.exit(1);
        }
    }

    private static void run(Args args) throws IOException {
        var config = ImmutableConfig.builder()
                .includedEventTypes(args.eventTypes)
                .build();
        var ctfFile = args.ctfFile == null
                ? args.jfrFile.resolveSibling(removeExtension(args.jfrFile.getFileName().toString()) + ".json")
                : args.ctfFile;
        new Jfr2CtfConverter(config).convert(args.jfrFile, ctfFile);
    }

    @Command(name = "jfr2ctf")
    static class Args {

        @Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
        boolean help;

        @Nullable
        @Option(names = {"--include-events"}, description = "events to include (defaults to all)")
        Set<String> eventTypes;

        @Parameters(arity = "1", index = "0", paramLabel = "JFR_FILE", description = "JFR file to convert")
        Path jfrFile;

        @Nullable
        @Parameters(arity = "0..1", index = "1", paramLabel = "CTF_FILE", description = "Result file to write (defaults to JFR_FILE with json extension)")
        Path ctfFile;

    }

}
