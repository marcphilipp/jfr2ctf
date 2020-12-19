package de.marcphilipp.jfr2ctf;

import jdk.jfr.consumer.RecordedEvent;
import org.immutables.value.Value.Immutable;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Set;
import java.util.function.Predicate;

@Immutable
interface RecordedEventFilter extends Predicate<RecordedEvent> {

    @Nullable
    Set<String> getIncludedEventTypes();

    @Nullable
    Duration getMinDuration();

    @Override
    default boolean test(RecordedEvent event) {
        return hasIncludedEventType(event)
                && hasIncludedDuration(event);
    }

    private boolean hasIncludedDuration(RecordedEvent event) {
        return getMinDuration() == null || event.getDuration().compareTo(getMinDuration()) >= 0;
    }

    private boolean hasIncludedEventType(RecordedEvent event) {
        return getIncludedEventTypes() == null || getIncludedEventTypes().contains(event.getEventType().getName());
    }
}
