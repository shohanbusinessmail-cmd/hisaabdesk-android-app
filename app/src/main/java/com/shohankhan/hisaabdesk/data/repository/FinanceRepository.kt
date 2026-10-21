package com.shohankhan.hisaabdesk.data.repository

import androidx.room.withTransaction
import com.shohankhan.hisaabdesk.core.money.Money
import com.shohankhan.hisaabdesk.data.local.*
import com.shohankhan.hisaabdesk.domain.accounting.*
import java.util.UUID

/** All multi-record financial writes are committed atomically with their ledger postings. */
class FinanceRepository(private val database: FinanceDatabase) {
    private val dao = database.financeDao()
    val balances = dao.observeBalances()
    val recentTransactions = dao.observeRecentTransactions()

    suspend fun createAccount(name: String, type: String = "CASH"): String {
        val cleanName = name.trim()
        require(cleanName.isNotEmpty()) { "হিসাবের নাম লিখুন।" }
        val id = UUID.randomUUID().toString()
        dao.insertAccount(AccountEntity(id, cleanName, type, createdAt = System.currentTimeMillis()))
        return id
    }

    suspend fun createTransaction(
        kind: OperationKind,
        amount: Money,
        sourceId: String? = null,
        destinationId: String? = null,
        category: String? = null,
        note: String? = null,
        occurredAt: Long = System.currentTimeMillis(),
        operationId: String = UUID.randomUUID().toString()
    ): String = database.withTransaction {
        dao.transactionByKey(operationId)?.let { return@withTransaction it.id }
        val operation = LedgerOperation(kind, amount, sourceId, destinationId)
        val postings = LedgerRules.postings(operation)
        postings.map { it.accountId }.distinct().forEach { accountId ->
            requireNotNull(dao.activeAccount(accountId)) { "নির্বাচিত হিসাবটি পাওয়া যায়নি বা বন্ধ আছে।" }
        }
        val id = UUID.randomUUID().toString()
        dao.insertTransaction(TransactionEntity(id, kind.name, amount.poisha, occurredAt, category?.trim()?.takeIf(String::isNotEmpty), note?.trim()?.takeIf(String::isNotEmpty), sourceId, destinationId, operationId))
        dao.insertEntries(postings.map { LedgerEntryEntity(UUID.randomUUID().toString(), id, it.accountId, it.delta.poisha) })
        id
    }

    suspend fun editTransaction(
        id: String,
        kind: OperationKind,
        amount: Money,
        sourceId: String?,
        destinationId: String?,
        category: String?,
        note: String?,
        occurredAt: Long
    ) = database.withTransaction {
        val old = requireNotNull(dao.transaction(id)) { "লেনদেনটি পাওয়া যায়নি।" }
        val postings = LedgerRules.postings(LedgerOperation(kind, amount, sourceId, destinationId))
        postings.map { it.accountId }.distinct().forEach { requireNotNull(dao.activeAccount(it)) { "হিসাবটি পাওয়া যায়নি।" } }
        dao.deleteEntries(id)
        dao.updateTransaction(old.copy(kind = kind.name, amountMinor = amount.poisha, occurredAt = occurredAt, category = category, note = note, sourceAccountId = sourceId, destinationAccountId = destinationId))
        dao.insertEntries(postings.map { LedgerEntryEntity(UUID.randomUUID().toString(), id, it.accountId, it.delta.poisha) })
    }

    suspend fun deleteTransaction(id: String) = database.withTransaction {
        requireNotNull(dao.transaction(id)) { "লেনদেনটি পাওয়া যায়নি।" }
        dao.deleteTransaction(id) // Entry rows are removed by the database's cascade in this same transaction.
    }

    suspend fun ensureInitialCashAccount(): Boolean {
        if (dao.accountCount() != 0) return false
        createAccount("নগদ", "CASH")
        return true
    }
}
