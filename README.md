# হিসাবডেস্ক (HisaabDesk)

Bengali-first, offline-oriented native Android finance app. This repository currently contains an early, working foundation: a Compose dashboard, local Room persistence, integer-poisha money parsing, and atomic income/expense/transfer ledger writes. It is **not yet a complete personal accounting product**; see [IMPLEMENTATION_STATUS.md](IMPLEMENTATION_STATUS.md) before using it for real financial records.

## Current build facts

- Application ID: `com.shohankhan.hisaabdesk` (debug builds use `.debug` suffix)
- Current version: `0.1.0` (`versionCode` 1)
- Android: `minSdk 26`, `targetSdk 35`, `compileSdk 35` (Android 8.0+ target range; broad device compatibility has not yet been tested)
- Kotlin 2.0.21, Android Gradle Plugin 8.7.3, Gradle 8.9, JDK 17, Jetpack Compose Material 3, Room 2.6.1
- All accounting records currently remain in the app's local SQLite database. No sign-in, cloud sync, analytics, or internet permission is included.
- Locale and interface are Bengali-first; amounts are stored as integer poisha and shown with `৳`.

## Implemented in this repository

- Actual Compose app shell with a clean light emerald palette and Bengali launcher name.
- On first launch, a zero-opening-balance নগদ account is created (not a sample transaction or invented balance).
- Create additional local accounts (currently cash-type UI only), record income, expense, and linked two-account transfer, and view a balance summary and latest 100 records.
- Room database with account, transaction, and signed ledger-posting tables, foreign keys, indices, unique operation-idempotency key, and atomic financial writes.
- Decimal input parsing into poisha with Bengali and Western digits, strict precision/grouping validation, and overflow-safe arithmetic.
- JVM unit tests for monetary parsing and core ledger invariants.
- GitHub Actions CI and tag-driven APK workflow.

## Build and test

Prerequisites: JDK 17 and Android SDK Platform 35/build tools. The Android Gradle Plugin downloads its SDK dependencies through Google/Maven repositories. The checked-in Gradle wrapper bootstraps Gradle 8.9.

```sh
./gradlew --version
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

Debug APK path: `app/build/outputs/apk/debug/app-debug.apk`. The debug application ID is `com.shohankhan.hisaabdesk.debug`. It can be installed with `adb install -r app/build/outputs/apk/debug/app-debug.apk` on a compatible device/emulator.

Run the same checks in CI by pushing to `main` or an `arena/**` branch, or opening a pull request to `main`. CI uploads the debug APK and test/lint reports as a workflow artifact.

## Release APK and signing

A tag such as `v0.1.0` triggers `.github/workflows/android-release.yml`. For a production-signed release, configure all repository Actions secrets:

- `ANDROID_KEYSTORE_BASE64` — base64 encoding of the existing JKS/PKCS12 keystore file.
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

Create the key once, keep an offline encrypted backup, and do not lose it: Android updates require the same application ID and signing identity. The workflow writes the decoded key only to the runner's temporary directory and does not print it. If all four secrets are not configured, the workflow deliberately publishes a clearly named **unsigned debug/testing-only APK**, not a production release. Configure signing secrets before distributing or updating a real installation. Never commit a keystore or passwords.

To encode a file locally for a GitHub secret, for example: `base64 -w 0 release-key.jks` (Linux); transfer the output securely into the repository's Actions secret settings. GitHub Actions usage limits depend on account type, repository visibility and current GitHub policy; no unlimited/free CI claim is made.

## Data, backup, and privacy

At this stage there is **no backup/export/import feature**. Database storage is local and Room/SQLite is not encrypted by this app. The current app does not provide a PIN lock. Do not treat the early build as protected storage for sensitive household records. Uninstalling or clearing app data removes the local database. See [PRIVACY.md](PRIVACY.md), [SECURITY.md](SECURITY.md), and [RELEASE.md](RELEASE.md).

## Repository map

- `app/src/main/java/.../core/money` — exact BDT amount representation/parser.
- `.../domain/accounting` — ledger posting rules.
- `.../data/local` — Room schema and DAO.
- `.../data/repository` — atomic persistence operations.
- `.../ui` — Bengali Compose dashboard and entry forms.
- `app/src/test` — domain/money tests.
- `.github/workflows` — CI and APK workflow.

## Current limitations

Shop credit, personal debts, NGO/bank loans, payments/allocations, savings goals, budgets, reports, search, category/tag administration, recurring items/reminders, receipts, CSV/JSON portability, PIN/biometrics, detailed account types/reconciliation, migration test coverage and instrumentation tests are not implemented. The present dashboard's total is the sum of active owned accounts, not net worth; there are no liabilities in this schema yet. Entries are dated at save time and cannot yet be edited/deleted from the UI. A zero-cost release signing identity has not been provisioned. No APK is claimed as built until a CI artifact/release is actually verified.
