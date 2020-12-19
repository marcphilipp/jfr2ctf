package de.marcphilipp.jfr2ctf;

import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordedThread;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

class ThreadMetadataEventConverter implements EventConverter {

    private final Set<Long> seenThreadIds = new HashSet<>();

    @Nullable
    @Override
    public ChromeTraceEvent apply(RecordedEvent event) {
        RecordedThread thread = event.getThread();
        if (thread != null) {
            var threadId = thread.getJavaThreadId();
            var threadName = ObjectUtils.firstNonNull(thread.getJavaName(), thread.getOSName());
            var threadGroup = thread.getThreadGroup();
            if (threadGroup != null && threadGroup.getName() != null) {
                threadName += " (" + threadGroup.getName() + ")";
            }
            if (seenThreadIds.add(threadId) && threadName != null) {
                return ImmutableChromeTraceEvent.builder()
                        .processId(PID)
                        .threadId(threadId)
                        .phaseType(ChromeTraceEvent.PhaseType.METADATA)
                        .name("thread_name")
                        .putArguments("name", threadName)
                        .build();
            }
        }
        return null;
    }
}
