package de.marcphilipp.jfr2ctf;

import jdk.jfr.EventType;
import picocli.CommandLine;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static org.apache.commons.lang3.StringUtils.substringBeforeLast;

public class Jfr2Ctf {

    public static void main(String[] args) throws Exception {
        CliArgs cliArgs = new CliArgs();
        try {
            new CommandLine(cliArgs).parseArgs(args);
            if (cliArgs.help) {
                CommandLine.usage(cliArgs, System.out);
                return;
            }
            new Jfr2Ctf().convertJfrToCtf(cliArgs);
        } catch (CommandLine.PicocliException e) {
            System.err.println(e.getMessage());
            System.err.println();
            CommandLine.usage(args, System.err);
            System.exit(1);
        }
    }

    private void convertJfrToCtf(CliArgs args) throws IOException {
        var eventTypeFilter = toEventTypeFilter(args);
        var eventConverters = List.of(new ThreadMetadataEventConverter(), new CompleteEventConverter());
        try (var reader = RecordedEventIterator.stream(args.jfrFile);
             var writer = newChromeTraceFileWriter(args)
        ) {
            reader
                    .filter(event -> eventTypeFilter.test(event.getEventType()))
                    .flatMap(event -> eventConverters.stream().map(it -> it.apply(event)).filter(Objects::nonNull))
                    .forEach(writer::write);
        }
    }

    private ChromeTraceFileWriter newChromeTraceFileWriter(CliArgs args) throws IOException {
        var file = args.ctfFile == null
                ? args.jfrFile.resolveSibling(substringBeforeLast(args.jfrFile.getFileName().toString(), ".") + ".json")
                : args.ctfFile;
        return new ChromeTraceFileWriter(file);
    }

    private Predicate<EventType> toEventTypeFilter(CliArgs args) {
        return args.eventTypes == null
                ? __ -> true
                : eventType -> args.eventTypes.contains(eventType.getName());
    }

}
