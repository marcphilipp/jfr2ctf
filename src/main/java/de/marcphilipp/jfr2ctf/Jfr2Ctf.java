package de.marcphilipp.jfr2ctf;

import de.marcphilipp.jfr2ctf.ChromeTraceEvent.PhaseType;
import jdk.jfr.EventType;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordedObject;
import jdk.jfr.consumer.RecordedThread;
import jdk.jfr.consumer.RecordedThreadGroup;
import jdk.jfr.consumer.RecordingFile;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import static java.time.temporal.ChronoUnit.MICROS;
import static org.apache.commons.lang3.StringUtils.substringBeforeLast;

@Command(name = "jfr2ctf")
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

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    boolean help;

    @Nullable
    @Option(names = {"--include-events"}, description = "events to include (defaults to all)")
    List<String> eventTypes;

    @Parameters(arity = "1", index = "0", paramLabel = "JFR_FILE", description = "JFR file to convert")
    Path jfrFile;

    @Parameters(arity = "0..1", index = "1", paramLabel = "CTF_FILE", description = "Result file to write (defaults to JFR_FILE with json extension)")
    Path ctfFile;

    public static void main(String[] args) throws Exception {
        Jfr2Ctf converter = new Jfr2Ctf();
        try {
            new CommandLine(converter).parseArgs(args);
            converter.convertJfrToCtf();
        } catch (CommandLine.PicocliException e) {
            System.err.println(e.getMessage());
            System.err.println();
            CommandLine.usage(converter, System.err);
            System.exit(1);
        }
    }

    private void convertJfrToCtf() throws IOException {
        if (help) {
            CommandLine.usage(this, System.out);
            return;
        }
        var eventTypeFilter = toEventTypeFilter();
        var ctfFile = this.ctfFile == null
                ? jfrFile.resolveSibling(substringBeforeLast(jfrFile.getFileName().toString(), ".") + ".json")
                : this.ctfFile;
        var seenThreadIds = new HashSet<>();
        long pid = 0;
        try (var reader = new RecordingFile(jfrFile); var writer = new ChromeTraceFileWriter(ctfFile)) {
            while (reader.hasMoreEvents()) {
                var event = reader.readEvent();
                if (eventTypeFilter.test(event.getEventType())) {
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
                            writer.write(ImmutableChromeTraceEvent.builder()
                                    .processId(pid)
                                    .threadId(threadId)
                                    .phaseType(PhaseType.METADATA)
                                    .name("thread_name")
                                    .putArguments("name", threadName)
                                    .build());
                        }
                    }
                    writer.write(toChromeTraceEvent(threadId, event));
                }
            }
        }
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

    private Predicate<EventType> toEventTypeFilter() {
        return eventTypes == null
                ? __ -> true
                : eventType -> eventTypes.contains(eventType.getName());
    }
}
