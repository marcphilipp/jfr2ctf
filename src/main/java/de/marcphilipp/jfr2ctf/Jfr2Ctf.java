package de.marcphilipp.jfr2ctf;

import de.marcphilipp.jfr2ctf.ChromeTraceEvent.PhaseType;
import jdk.jfr.EventType;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordedObject;
import jdk.jfr.consumer.RecordedThread;
import jdk.jfr.consumer.RecordedThreadGroup;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.MICROS;
import static org.apache.commons.lang3.StringUtils.substringBeforeLast;

public class Jfr2Ctf {

    private static final Map<String, BiFunction<RecordedEvent, String, Object>> SUPPORTED_VALUE_TYPES = Map.of(
            Boolean.TYPE.getName(), RecordedObject::getBoolean,
            Byte.TYPE.getName(), RecordedObject::getByte,
            Short.TYPE.getName(), RecordedObject::getShort,
            Integer.TYPE.getName(), RecordedObject::getInt,
            Float.TYPE.getName(), RecordedObject::getFloat,
            Long.TYPE.getName(), RecordedObject::getLong,
            Double.TYPE.getName(), RecordedObject::getDouble,
            Duration.class.getName(), (recordedEvent, name) -> recordedEvent.getDuration(name).toString(),
            String.class.getName(), RecordedObject::getString
    );

    private static final Set<String> EXCLUDED_VALUE_NAMES = Set.of("startTime", "duration");

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
        var seenThreadIds = new HashSet<Long>();
        try (var reader = RecordedEventIterator.stream(args.jfrFile);
             var writer = newChromeTraceFileWriter(args)
        ) {
            reader
                    .filter(event -> eventTypeFilter.test(event.getEventType()))
                    .flatMap(event -> convertEvent(event, seenThreadIds))
                    .forEach(writer::write);
        }
    }

    private ChromeTraceFileWriter newChromeTraceFileWriter(CliArgs args) throws IOException {
        var file = args.ctfFile == null
                ? args.jfrFile.resolveSibling(substringBeforeLast(args.jfrFile.getFileName().toString(), ".") + ".json")
                : args.ctfFile;
        return new ChromeTraceFileWriter(file);
    }

    private Stream<ChromeTraceEvent> convertEvent(RecordedEvent event, Set<Long> seenThreadIds) {
        var result = new ArrayList<ChromeTraceEvent>();
        Long threadId = null;
        RecordedThread thread = event.getThread();
        if (thread != null) {
            threadId = thread.getJavaThreadId();
            String threadName = ObjectUtils.firstNonNull(thread.getJavaName(), thread.getOSName());
            RecordedThreadGroup threadGroup = thread.getThreadGroup();
            if (threadGroup != null && threadGroup.getName() != null) {
                threadName += " (" + threadGroup.getName() + ")";
            }
            if (seenThreadIds.add(threadId) && threadName != null) {
                result.add(ImmutableChromeTraceEvent.builder()
                        .processId(0)
                        .threadId(threadId)
                        .phaseType(PhaseType.METADATA)
                        .name("thread_name")
                        .putArguments("name", threadName)
                        .build());
            }
        }
        result.add(toChromeTraceEvent(threadId, event));
        return result.stream();
    }

    private ChromeTraceEvent toChromeTraceEvent(Long threadId, RecordedEvent event) {
        var chromeTraceEvent = ImmutableChromeTraceEvent.builder()
                .processId(0)
                .threadId(threadId)
                .phaseType(PhaseType.COMPLETE)
                .name(event.getEventType().getLabel())
                .categories(String.join(",", event.getEventType().getCategoryNames()))
                .timestamp(MICROS.between(Instant.EPOCH, event.getStartTime()))
                .duration(TimeUnit.NANOSECONDS.toMicros(event.getDuration().toNanos()));
        event.getFields().stream()
                .filter(desc -> SUPPORTED_VALUE_TYPES.containsKey(desc.getTypeName()))
                .filter(desc -> !EXCLUDED_VALUE_NAMES.contains(desc.getName()))
                .forEach(desc -> {
                    Object value = SUPPORTED_VALUE_TYPES.get(desc.getTypeName()).apply(event, desc.getName());
                    if (value != null) {
                        chromeTraceEvent.putArguments(desc.getName(), value);
                    }
                });
        return chromeTraceEvent.build();
    }

    private Predicate<EventType> toEventTypeFilter(CliArgs args) {
        return args.eventTypes == null
                ? __ -> true
                : eventType -> args.eventTypes.contains(eventType.getName());
    }
}
