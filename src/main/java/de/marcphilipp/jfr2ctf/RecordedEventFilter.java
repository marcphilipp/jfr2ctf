package de.marcphilipp.jfr2ctf;

import jdk.jfr.consumer.RecordedEvent;
import org.immutables.value.Value.Immutable;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Predicate;

@Immutable
interface RecordedEventFilter extends Predicate<RecordedEvent> {

    @Nullable
    Set<String> getIncludedEventTypes();

    @Override
    default boolean test(RecordedEvent event) {
        return getIncludedEventTypes() == null || getIncludedEventTypes().contains(event.getEventType().getName());
    }
}
