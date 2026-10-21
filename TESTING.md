# Testing and verification

## Automated checks in the source tree

- `MoneyTest` covers BDT poisha conversion, Bengali/Western digits, decimal precision, comma grouping, malformed input, overflow, income/expense signs, and equal/opposite transfer postings.
- The GitHub Actions CI job runs `./gradlew testDebugUnitTest`, `./gradlew lintDebug`, and `./gradlew assembleDebug`; it uploads APK and reports even on failure where files exist.
- The current branch has no Room in-memory/database instrumentation tests, migration test, Compose UI test, emulator test, APK installation test, static security analysis, or loan/backup/PIN tests. Do not treat those as passing.

## Commands

```sh
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

The developer environment for the initial implementation inspection had no Java runtime, Android SDK, or existing Gradle wrapper. Consequently no local Gradle compile or test run could be performed in this environment. The actual CI run is the source of build/test status once the branch workflow executes; record its URL and result in `IMPLEMENTATION_STATUS.md` after inspection.
