package de.marcphilipp.jfr2ctf;

import jdk.jfr.consumer.RecordedEvent;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Predicate;

class Jfr2CtfConverter {

    private final Predicate<RecordedEvent> recordedEventFilter;

    Jfr2CtfConverter(Predicate<RecordedEvent> recordedEventFilter) {
        this.recordedEventFilter = recordedEventFilter;
    }

    void convert(Path inputFile, Path outputFile) throws IOException {
        var compositeEventConverter = EventConverter.composite(
                new ThreadMetadataEventConverter(),
                new CompleteEventConverter()
        );
        try (var recordedEvents = RecordedEventIterator.stream(inputFile);
             var chromeTraceFileWriter = new ChromeTraceFileWriter(outputFile)
        ) {
            recordedEvents
                    .filter(recordedEventFilter)
                    .flatMap(compositeEventConverter)
                    .forEach(chromeTraceFileWriter::write);
        }
    }

}
