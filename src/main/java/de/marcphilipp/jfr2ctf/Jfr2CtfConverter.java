package de.marcphilipp.jfr2ctf;

import jdk.jfr.consumer.RecordedEvent;
import org.apache.commons.lang3.mutable.MutableLong;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Predicate;

class Jfr2CtfConverter {

    private final Predicate<RecordedEvent> recordedEventFilter;

    Jfr2CtfConverter(Predicate<RecordedEvent> recordedEventFilter) {
        this.recordedEventFilter = recordedEventFilter;
    }

    Result convert(Path inputFile, Path outputFile) throws IOException {
        var compositeEventConverter = EventConverter.composite(
                new ThreadMetadataEventConverter(),
                new CompleteEventConverter()
        );
        var totalEvents = new MutableLong(0);
        var includedEvents = new MutableLong(0);
        try (var recordedEvents = RecordedEventIterator.stream(inputFile);
             var chromeTraceFileWriter = new ChromeTraceFileWriter(outputFile)
        ) {
            recordedEvents
                    .peek(__ -> totalEvents.increment())
                    .filter(recordedEventFilter)
                    .peek(__ -> includedEvents.increment())
                    .flatMap(compositeEventConverter)
                    .forEach(chromeTraceFileWriter::write);
        }
        return new Result(includedEvents.longValue(), totalEvents.longValue());
    }

    record Result(long includedEvents, long totalEvents) {}

}
