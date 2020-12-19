package de.marcphilipp.jfr2ctf;

import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.fasterxml.jackson.core.JsonGenerator.Feature.AUTO_CLOSE_TARGET;
import static com.fasterxml.jackson.core.JsonGenerator.Feature.FLUSH_PASSED_TO_STREAM;

class ChromeTraceFileWriter implements Closeable {

    private final JsonMapper jsonMapper = JsonMapper.builder()
            .disable(AUTO_CLOSE_TARGET)
            .disable(FLUSH_PASSED_TO_STREAM)
            .build();

    private final BufferedWriter out;
    private boolean first = true;

    ChromeTraceFileWriter(Path file) throws IOException {
        out = Files.newBufferedWriter(file);
        out.write("[");
    }

    void write(ChromeTraceEvent event) {
        try {
            if (first) {
                first = false;
            } else {
                out.write(",\n");
            }
            jsonMapper.writeValue(out, event);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            out.write("]\n");
        } finally {
            out.close();
        }
    }
}
