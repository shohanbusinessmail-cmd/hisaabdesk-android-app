package com.shohankhan.hisaabdesk.domain.accounting

import com.shohankhan.hisaabdesk.core.money.Money

/** Signed account postings; positive values increase an owned account, negative values decrease it. */
data class Posting(val accountId: String, val delta: Money)

enum class OperationKind { INCOME, EXPENSE, TRANSFER, OPENING_BALANCE, ADJUSTMENT }

data class LedgerOperation(val kind: OperationKind, val amount: Money, val fromId: String?, val toId: String?)

object LedgerRules {
    fun postings(operation: LedgerOperation): List<Posting> {
        require(operation.amount.poisha > 0L) { "অঙ্ক শূন্যের বেশি হতে হবে।" }
        return when (operation.kind) {
            OperationKind.INCOME -> listOf(Posting(requireNotNull(operation.toId), operation.amount))
            OperationKind.EXPENSE -> listOf(Posting(requireNotNull(operation.fromId), Money(Math.negateExact(operation.amount.poisha))))
            OperationKind.TRANSFER -> {
                val from = requireNotNull(operation.fromId)
                val to = requireNotNull(operation.toId)
                require(from != to) { "একই হিসাবে স্থানান্তর করা যাবে না।" }
                listOf(Posting(from, Money(Math.negateExact(operation.amount.poisha))), Posting(to, operation.amount))
            }
            OperationKind.OPENING_BALANCE -> listOf(Posting(requireNotNull(operation.toId), operation.amount))
            OperationKind.ADJUSTMENT -> listOf(Posting(requireNotNull(operation.toId), operation.amount))
        }
    }

    fun balance(openingBalance: Money, postings: Iterable<Posting>, accountId: String): Money =
        postings.asSequence().filter { it.accountId == accountId }.fold(openingBalance) { total, posting -> total + posting.delta }

    fun total(postings: Iterable<Posting>): Money = postings.fold(Money.ZERO) { sum, posting -> sum + posting.delta }
}
