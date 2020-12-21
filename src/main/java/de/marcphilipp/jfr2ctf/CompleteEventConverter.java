package de.marcphilipp.jfr2ctf;

import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordedObject;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import static java.time.temporal.ChronoUnit.MICROS;

class CompleteEventConverter implements EventConverter {

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

    @Nullable
    @Override
    public ChromeTraceEvent apply(RecordedEvent event) {
        return ChromeTraceEventBuilder.builder()
                .processId(PID)
                .threadId(event.getThread() == null ? null : event.getThread().getJavaThreadId())
                .phaseType(ChromeTraceEvent.PhaseType.COMPLETE)
                .name(event.getEventType().getLabel())
                .categories(String.join(",", event.getEventType().getCategoryNames()))
                .timestamp(MICROS.between(Instant.EPOCH, event.getStartTime()))
                .duration(TimeUnit.NANOSECONDS.toMicros(event.getDuration().toNanos()))
                .arguments(toArguments(event))
                .build();
    }

    private Map<String, Object> toArguments(RecordedEvent event) {
        var arguments = new HashMap<String, Object>();
        arguments.put("eventType", event.getEventType().getName());
        event.getFields().stream()
                .filter(desc -> SUPPORTED_VALUE_TYPES.containsKey(desc.getTypeName()))
                .filter(desc -> !EXCLUDED_VALUE_NAMES.contains(desc.getName()))
                .forEach(desc -> {
                    Object value = SUPPORTED_VALUE_TYPES.get(desc.getTypeName()).apply(event, desc.getName());
                    if (value != null) {
                        arguments.put(desc.getName(), value);
                    }
                });
        return arguments;
    }
}
