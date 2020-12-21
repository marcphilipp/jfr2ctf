package de.marcphilipp.jfr2ctf;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.soabase.recordbuilder.core.RecordBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@RecordBuilder
@JsonInclude(NON_EMPTY)
record ChromeTraceEvent(

        @Nullable
        @JsonProperty("name")
        String name,

        @JsonProperty("ph")
        PhaseType phaseType,

        @Nullable
        @JsonProperty("ts")
        Long timestamp,

        @Nullable
        @JsonProperty("dur")
        Long duration,

        @JsonProperty("pid")
        long processId,

        @Nullable
        @JsonProperty("tid")
        Long threadId,

        @Nullable
        @JsonProperty("cat")
        String categories,

        @JsonProperty("args")
        Map<String, Object> arguments

) {

    enum PhaseType {
        METADATA("M"),
        COMPLETE("X");

        private final String jsonValue;

        PhaseType(String jsonValue) {
            this.jsonValue = jsonValue;
        }

        @SuppressWarnings("unused")
        @JsonValue
        public String getJsonValue() {
            return jsonValue;
        }
    }
}
