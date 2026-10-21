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

The developer environment had no Java runtime or Android SDK, so local Gradle checks and device installation were unavailable. GitHub Actions run [39571785202](https://github.com/shohanbusinessmail-cmd/hisaabdesk-android-app/actions/runs/39571785202) passed `testDebugUnitTest`, `lintDebug`, `assembleDebug`, and artifact upload for commit `1b8a7b2972b826f98ebd8a236f6cfa340d4bb3a8`. The uploaded artifact exists, but its archive could not be downloaded by the agent because the API redirects to an external blob host. Database integration and device/UI tests remain unimplemented. See `IMPLEMENTATION_STATUS.md` for the test ledger.
