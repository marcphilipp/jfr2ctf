package de.marcphilipp.jfr2ctf;

import jdk.jfr.EventType;
import org.immutables.value.Value.Immutable;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

class Jfr2CtfConverter {

    private final Predicate<EventType> eventTypeFilter;

    Jfr2CtfConverter(Config config) {
        this.eventTypeFilter = toEventTypeFilter(config);
    }

    private static Predicate<EventType> toEventTypeFilter(Config config) {
        return config.getIncludedEventTypes() == null
                ? __ -> true
                : eventType -> config.getIncludedEventTypes().contains(eventType.getName());
    }

    void convert(Path inputFile, Path outputFile) throws IOException {
        var eventConverters = List.of(new ThreadMetadataEventConverter(), new CompleteEventConverter());
        try (var recordedEvents = RecordedEventIterator.stream(inputFile);
             var chromeTraceFileWriter = new ChromeTraceFileWriter(outputFile)
        ) {
            recordedEvents
                    .filter(event -> eventTypeFilter.test(event.getEventType()))
                    .flatMap(event -> EventConverter.applyAll(eventConverters, event))
                    .forEach(chromeTraceFileWriter::write);
        }
    }

    @Immutable
    interface Config {
        Set<String> getIncludedEventTypes();
    }

}
