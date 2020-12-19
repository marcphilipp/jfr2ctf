package de.marcphilipp.jfr2ctf;

import jdk.jfr.EventType;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordedThread;
import org.immutables.value.Value.Immutable;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@Immutable
interface RecordedEventFilter extends Predicate<RecordedEvent> {

    @Nullable
    Set<String> getIncludedEventTypes();

    @Nullable
    Set<Pattern> getExcludedThreadNames();

    @Nullable
    Duration getMinDuration();

    @Override
    default boolean test(RecordedEvent event) {
        return isIncluded(event.getEventType())
                && !isExcluded(event.getThread())
                && isIncluded(event.getDuration());
    }

    private boolean isIncluded(EventType eventType) {
        return getIncludedEventTypes() == null || getIncludedEventTypes().contains(eventType.getName());
    }

    private boolean isExcluded(RecordedThread thread) {
        return getExcludedThreadNames() != null
                && thread != null
                && thread.getJavaName() != null
                && getExcludedThreadNames().stream().anyMatch(it -> it.matcher(thread.getJavaName()).matches());
    }

    private boolean isIncluded(Duration duration) {
        return getMinDuration() == null || duration.compareTo(getMinDuration()) >= 0;
    }
}
