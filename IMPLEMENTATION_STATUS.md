# Implementation status

Updated: 2026-10-21

## Repository and build environment

- Repository: [shohanbusinessmail-cmd/hisaabdesk-android-app](https://github.com/shohanbusinessmail-cmd/hisaabdesk-android-app), public; GitHub identity reports `ADMIN` permission. Default branch is `main`; work is on `arena/722a7df0-hisaabdesk-android-app`.
- Initial tracked state: README only, commit `c554367` (`Initial commit`); working tree was clean at start.
- Local environment inspection: Debian 12, Python 3.11.2; `java`, Android SDK/`adb`, and pre-existing `gradlew` absent. A Gradle 8.9 wrapper and Android project configuration have now been added, but a local Android build cannot run until a JDK and Android SDK are available.
- A debug APK, release APK, emulator installation, or release has **not** yet been verified. Do not provide a download link until a workflow artifact/release asset is inspected.

## Implemented in source (not yet build-verified)

- Kotlin/Compose single-module Android application: ID `com.shohankhan.hisaabdesk`, version `0.1.0`/code 1, min SDK 26, target/compile SDK 35.
- Bengali Light Compose dashboard and launcher artwork; app creates an empty zero-balance `নগদ` account if there are no accounts.
- Integer-poisha `Money` parser/formatter with Bengali digits, strict precision/grouping, and overflow checks.
- Pure operation/posting rules for income, expenses, transfers, and initial balance concept; transfer is equal/opposite and not classified as income/expense.
- Room v1 account/transaction/ledger tables; foreign keys/indexes, unique idempotency key, SQL-derived account balances, reactive reads, and atomic transaction create/edit/delete repository operations.
- Compose create forms for accounts and income/expense/transfers; recent 100 database transactions.
- Unit test source for money and ledger logic.
- CI and tag release workflows; unsigned-debug fallback is explicitly labelled.
- Project documentation: README, architecture, financial rules, security, privacy, testing, release, changelog, and this status.

## Implemented but not fully verified

- Android/Compose/Room compilation and resource linking: source/configuration added; awaits CI because local JDK/SDK absent.
- Financial unit tests: written, not executed in this environment.
- Database atomic operations: implementation present, no Room database integration tests yet.
- CI/release workflows: files added, no workflow run inspected yet.

## Partial / omitted (not available to users as completed functionality)

- UI entry forms do not yet expose custom date selection, edits, reversals, deletion, opening-balance setup, deposits/withdrawals, account type selection, reconciliation, categories beyond free-text labels, filters, or reports.
- Database currently contains only owned accounts, transactions, and ledger postings; there is no debt/liability, contact, shop, loan, savings goal, budget, recurring schedule, reminder, receipt, tag, or audit-history schema.
- No PIN or biometric lock, database encryption, recovery, JSON/CSV backup/import, search, or notification support.
- No custom Bengali/Western numeral preference, accessibility/UI instrumentation, documented migration test, screenshot, or device-specific Bengali font verification.
- Dashboard total is owned account balance only, not net worth; no liability figures or fabricated sample transactions are shown.

## Test and release record

- Local Gradle command: **not run** — JDK/Android SDK were missing at initial environment inspection.
- Unit tests: **not run** locally (source present).
- Room instrumentation/database tests: **not implemented/not run**.
- Compose UI/emulator tests and install: **not implemented/not run**.
- CI run/status/artifact: **pending** workflow execution and inspection.
- APK/checksum/signature/release URL: **not available/verified yet**.
- Signing secrets/key continuity: **not configured or verified**. No signing key is in source control.

## Next exact steps

1. Validate source compilation, test and lint on GitHub Actions; inspect logs and correct all build failures.
2. Add in-memory Room integration tests for atomic transfer, idempotency, cascade/edit, and balance reconciliation; run on CI.
3. Add categorized transaction model/date semantics and UI editing/reversal with tests before publishing a real-finance build.
4. Continue modules in order: account types/opening balances → shop credit and personal debts → defensible NGO/bank loans → savings/budgets/reports → lock/backup → reminders and full UI.
5. Set up one permanent release keystore securely, run release workflow, verify APK package/signature/checksum, and only then report real artifact links.
