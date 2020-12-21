package de.marcphilipp.jfr2ctf;

import org.jetbrains.annotations.Nullable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.PicocliException;

import java.io.IOException;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.Set;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toSet;
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
        var filter = RecordedEventFilterBuilder.builder()
                .includedEventTypes(args.includedEventTypes)
                .excludedThreadNames(args.excludedThreadNames == null
                        ? null
                        : args.excludedThreadNames.stream().map(Pattern::compile).collect(toSet()))
                .minDuration(args.minDurationMillis == null
                        ? null
                        : Duration.ofMillis(args.minDurationMillis))
                .build();
        var ctfFile = args.ctfFile == null
                ? args.jfrFile.resolveSibling(removeExtension(args.jfrFile.getFileName().toString()) + ".json")
                : args.ctfFile;
        var result = new Jfr2CtfConverter(filter).convert(args.jfrFile, ctfFile);
        System.out.println(MessageFormat.format("Wrote Chrome trace file to {0} including {1} of {2} events",
                ctfFile,
                result.includedEvents(),
                result.totalEvents()));
    }

    @Command(name = "jfr2ctf")
    static class Args {

        @Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
        boolean help;

        @Nullable
        @Option(names = {"--include-events"}, description = "event types to include (defaults to all)")
        Set<String> includedEventTypes;

        @Nullable
        @Option(names = {"--exclude-threads"}, description = "thread names to exclude (defaults to none) [regex]")
        Set<String> excludedThreadNames;

        @Nullable
        @Option(names = {"--min-duration"}, description = "minimum event duration (in millis)")
        Long minDurationMillis;

        @Parameters(arity = "1", index = "0", paramLabel = "JFR_FILE", description = "JFR file to convert")
        Path jfrFile;

        @Nullable
        @Parameters(arity = "0..1", index = "1", paramLabel = "CTF_FILE", description = "Result file to write (defaults to JFR_FILE with json extension)")
        Path ctfFile;

    }

}
