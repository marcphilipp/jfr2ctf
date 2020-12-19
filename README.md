# jfr2ctf

Command-line tool to convert a JFR recording to a Chrome trace file (CTF).

## Usage

```sh
./gradlew run --args="[-h] [--include-events=<eventTypes>]... JFR_FILE [CTF_FILE]"
      JFR_FILE     JFR file to convert
      [CTF_FILE]   Result file to write (defaults to JFR_FILE with json
                     extension)
  -h, --help       display this help message
      --include-events=<eventTypes>
                   events to include (defaults to all)
```

## Viewing

Open [about:tracing](about:tracing) in a Chromium-based browser and open the resulting JSON file.
