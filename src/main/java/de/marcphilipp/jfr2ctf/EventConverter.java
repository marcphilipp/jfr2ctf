package de.marcphilipp.jfr2ctf;

import jdk.jfr.consumer.RecordedEvent;

interface EventConverter {

    long PID = 0;

    ChromeTraceEvent apply(RecordedEvent event);
}
