package de.marcphilipp.jfr2ctf;

import jdk.jfr.EventType;
import picocli.CommandLine;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

public class Jfr2Ctf {

    public static void main(String[] args) throws Exception {
        CliArgs cliArgs = new CliArgs();
        try {
            new CommandLine(cliArgs).parseArgs(args);
            if (cliArgs.help) {
                CommandLine.usage(cliArgs, System.out);
                return;
            }
            convertJfrToCtf(cliArgs);
        } catch (CommandLine.PicocliException e) {
            System.err.println(e.getMessage());
            System.err.println();
            CommandLine.usage(args, System.err);
            System.exit(1);
        }
    }

    private static void convertJfrToCtf(CliArgs args) throws IOException {
        var eventTypeFilter = toEventTypeFilter(args);
        var eventConverters = List.of(new ThreadMetadataEventConverter(), new CompleteEventConverter());
        try (var recordedEvents = RecordedEventIterator.stream(args.jfrFile);
             var chromeTraceFileWriter = new ChromeTraceFileWriter(args.resolveCtfFile())
        ) {
            recordedEvents
                    .filter(event -> eventTypeFilter.test(event.getEventType()))
                    .flatMap(event -> EventConverter.applyAll(eventConverters, event))
                    .forEach(chromeTraceFileWriter::write);
        }
    }

    private static Predicate<EventType> toEventTypeFilter(CliArgs args) {
        return args.eventTypes == null
                ? __ -> true
                : eventType -> args.eventTypes.contains(eventType.getName());
    }

}
