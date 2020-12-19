package de.marcphilipp.jfr2ctf;

import jdk.jfr.consumer.RecordedEvent;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

interface EventConverter {

    long PID = 0;

    static Function<RecordedEvent, Stream<ChromeTraceEvent>> composite(EventConverter... converters) {
        return event -> Stream.of(converters)
                .map(it -> it.apply(event))
                .filter(Objects::nonNull);
    }

    ChromeTraceEvent apply(RecordedEvent event);
}
