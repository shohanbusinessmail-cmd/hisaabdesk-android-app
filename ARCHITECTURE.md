# Architecture

## Shape

Single Android module with a small separation of concerns:

- `core/money`: BDT in integer poisha, strict parsing, formatting isolated from calculation.
- `domain/accounting`: pure operation-to-posting rules and overflow-safe balance aggregation.
- `data/local`: Room entities, foreign keys, DAO queries, database creation.
- `data/repository`: transaction boundary and operation validation; UI does not write ledger tables directly.
- `ui`: Compose screens driven by Room `Flow`s and repository commands.

There is no network client, user account, or remote dependency. Currency is currently fixed to BDT. Transaction timestamps are Unix epoch milliseconds (UTC instant); display uses Android's device locale/time zone. The entry form records the current instant and does not yet provide a user-selected date/time.

## Source of truth and consistency

`accounts.openingBalanceMinor` represents an opening figure separately from income. The current account balance query is opening balance plus the sum of its signed `ledger_entries`. Each successful transaction and its postings are inserted inside one Room `withTransaction` block. Transfers use equal-and-opposite entries. Ledger posting rows cascade when a transaction is removed; account deletion is restricted by the foreign key (account archiving is reserved in schema but not currently exposed). A unique operation key prevents retrying the same submitted operation from posting twice.

The transaction UI now provides create/edit and confirmed hard delete for standalone entries. The repository edit/delete routines are atomic for this three-table model but have not yet had database integration/regression tests. Deletion cascades ledger rows and is not an audit-preserving reversal/history feature; do not interpret this as complete production accounting history. The operation model does not yet represent liabilities, payment allocations, reversals, or multi-currency.

## Database and migration policy

Room schema version 1. Schema export is directed to `app/schemas`; migration and destructive-fallback code is intentionally not configured. Future schema changes must add and test explicit migrations before a release that contains user data. A version-1 to version-1 migration path does not exist yet because this is the initial schema.

## Reactive reads

The dashboard observes SQL-aggregated account balances and the 100 most recent transactions using Flow. It does not load all history. There are no advanced reports or paged search queries yet.

## Build stack

AGP 8.7.3, Gradle 8.9, Kotlin 2.0.21 with its Compose compiler plugin, JDK 17, compile/target SDK 35, min SDK 26. Library versions are specified in `app/build.gradle.kts`.
