package de.marcphilipp.jfr2ctf;

import jdk.jfr.EventType;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordedThread;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

record RecordedEventFilter(@Nullable Set<String> includedEventTypes, @Nullable Set<Pattern> excludedThreadNames, @Nullable Duration minDuration) implements Predicate<RecordedEvent> {

    @Override
    public boolean test(RecordedEvent event) {
        return isIncluded(event.getEventType())
                && !isExcluded(event.getThread())
                && isIncluded(event.getDuration());
    }

    private boolean isIncluded(EventType eventType) {
        return includedEventTypes == null || includedEventTypes.contains(eventType.getName());
    }

    private boolean isExcluded(RecordedThread thread) {
        return excludedThreadNames != null
                && thread != null
                && thread.getJavaName() != null
                && excludedThreadNames.stream().anyMatch(it -> it.matcher(thread.getJavaName()).matches());
    }

    private boolean isIncluded(Duration duration) {
        return minDuration == null || duration.compareTo(minDuration) >= 0;
    }
}
