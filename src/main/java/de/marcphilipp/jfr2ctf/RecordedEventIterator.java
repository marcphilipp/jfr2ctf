package de.marcphilipp.jfr2ctf;

import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class RecordedEventIterator implements Iterator<RecordedEvent> {

    public static Stream<RecordedEvent> stream(Path file) throws IOException {
        var recording = new RecordingFile(file);
        var iterator = new RecordedEventIterator(recording);
        var spliterator = Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED);
        return StreamSupport.stream(spliterator, false).onClose(() -> {
            try {
                recording.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    private final RecordingFile recordingFile;

    private RecordedEventIterator(RecordingFile recordingFile) {
        this.recordingFile = recordingFile;
    }

    @Override
    public boolean hasNext() {
        return recordingFile.hasMoreEvents();
    }

    @Override
    public RecordedEvent next() {
        try {
            return recordingFile.readEvent();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
