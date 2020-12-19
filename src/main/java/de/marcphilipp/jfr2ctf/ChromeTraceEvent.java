package de.marcphilipp.jfr2ctf;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import org.immutables.value.Value.Immutable;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@Immutable
@JsonInclude(NON_EMPTY)
interface ChromeTraceEvent {

    @Nullable
    @JsonProperty("name")
    String getName();

    @JsonProperty("ph")
    PhaseType getPhaseType();

    @Nullable
    @JsonProperty("ts")
    Long getTimestamp();

    @Nullable
    @JsonProperty("dur")
    Long getDuration();

    @JsonProperty("pid")
    long getProcessId();

    @Nullable
    @JsonProperty("id")
    Long getId();

    @Nullable
    @JsonProperty("tid")
    Long getThreadId();

    @Nullable
    @JsonProperty("cat")
    String getCategories();

    @JsonProperty("args")
    Map<String, Object> getArguments();

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
