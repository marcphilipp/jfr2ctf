# jfr2ctf

Command-line tool to convert a JFR recording to a Chrome trace file (CTF).

## Getting Started

Convert JFR file with default options

```sh
./gradlew run --args="<JFR_FILE>"

> Task :run
Wrote Chrome trace file to /some/path/recording.json

BUILD SUCCESSFUL in 1s
2 actionable tasks: 1 executed, 1 up-to-date
```

## Usage options
```sh
./gradlew run "--args=-h" 

> Task :run
Usage: jfr2ctf [-h] [--min-duration=<minDurationMillis>]
               [--exclude-threads=<excludedThreadNames>]...
               [--include-events=<includedEventTypes>]... JFR_FILE [CTF_FILE]
      JFR_FILE     JFR file to convert
      [CTF_FILE]   Result file to write (defaults to JFR_FILE with json
                     extension)
      --exclude-threads=<excludedThreadNames>
                   thread names to exclude (defaults to none) [regex]
  -h, --help       display this help message
      --include-events=<includedEventTypes>
                   event types to include (defaults to all)
      --min-duration=<minDurationMillis>
                   minimum event duration (in millis)

BUILD SUCCESSFUL in 933ms
2 actionable tasks: 1 executed, 1 up-to-date
```

## Viewing

Open [about:tracing](about:tracing) in a Chromium-based browser and open the resulting JSON file.
