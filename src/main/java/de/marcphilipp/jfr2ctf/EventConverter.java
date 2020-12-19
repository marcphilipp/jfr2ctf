package de.marcphilipp.jfr2ctf;

import jdk.jfr.consumer.RecordedEvent;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

interface EventConverter {

    long PID = 0;

    static Stream<ChromeTraceEvent> applyAll(List<EventConverter> eventConverters, RecordedEvent event) {
        return eventConverters.stream()
                .map(it -> it.apply(event))
                .filter(Objects::nonNull);
    }

    ChromeTraceEvent apply(RecordedEvent event);
}
