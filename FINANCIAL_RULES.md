# Financial rules (implemented scope)

## Currency and precision

- Currency: BDT; one taka is 100 poisha.
- Authoritative amounts are signed `Long` poisha. `Float`/`Double` are not used for accounting values.
- User-entered decimal taka are parsed with decimal arithmetic, at most two fractional digits, exact conversion, and overflow checks. Bengali digits are normalized to Western digits. Commas are accepted only as valid groups of three. Negative/zero transaction amounts are rejected by the ledger operation validator.
- Formatting is a presentation concern and uses the Bengali Bangladesh locale by default. Formatting does not feed back into a calculation.
- No interest, tax, fee, or installment schedule calculation is implemented.

## Current ledger semantics

A transaction amount is positive in the transaction table. Its ledger entries are signed deltas to owned account balances:

| Operation | Account posting | Income/expense report treatment |
|---|---|---|
| Income | Destination `+amount` | Income (future reports must include once) |
| Expense | Source `-amount` | Expense (future reports must include once) |
| Transfer | Source `-amount`, destination `+amount` | Neither income nor expense |
| Opening balance | Separate account opening value, not an income transaction | Neither income nor expense |
| Adjustment | Domain supports a signed posting concept, but user flow is not available | Must be separately identified, never silently classified |

A transfer requires distinct existing active account IDs. The pair is inserted atomically; summing both postings gives zero. Account balances are derived from opening balance plus signed ledger rows, not from a mutable cached balance. Negative account balances are not universally prohibited.

The current app has no liability/receivable model, so it cannot yet compute net worth. A dashboard sum is account assets only and must not be described as net worth.

## Retry, edit and removal

An operation ID is unique in the transaction table. Retrying a create request with the same key returns the original transaction ID rather than posting again. Repository edit removes prior postings and creates new postings in a database transaction. The UI exposes editing and confirmed hard deletion; delete cascades its postings, avoiding orphans. This is not audit-preserving reversal behavior, and database integration tests remain future work.

## Classification and excluded totals

Transfers are not income or expense. Loan principal, debt settlement, shop credit, interest, fees, refunds and reversals are not implemented in this release baseline. Once added, principal must remain balance-sheet movement; interest/charges must be separately classified and a payment must not be counted twice. No screen currently publishes income/expense report totals.

## Dates

A transaction stores an epoch-millisecond instant. New entries use the current instant. Device-local formatting can display an instant differently after a time-zone change; a user-selected accounting calendar date is not yet separately preserved. Date-boundary behavior and reporting ranges have not yet been implemented/tested.
