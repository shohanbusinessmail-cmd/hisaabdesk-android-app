package com.shohankhan.hisaabdesk.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "accounts", indices = [Index("archived")])
data class AccountEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    val openingBalanceMinor: Long = 0,
    val archived: Boolean = false,
    val createdAt: Long
)

@Entity(tableName = "transactions", indices = [Index("occurredAt"), Index("kind"), Index(value = ["idempotencyKey"], unique = true)])
data class TransactionEntity(
    @PrimaryKey val id: String,
    val kind: String,
    val amountMinor: Long,
    val occurredAt: Long,
    val category: String?,
    val note: String?,
    val sourceAccountId: String?,
    val destinationAccountId: String?,
    val idempotencyKey: String
)

@Entity(
    tableName = "ledger_entries",
    foreignKeys = [
        ForeignKey(entity = TransactionEntity::class, parentColumns = ["id"], childColumns = ["transactionId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = AccountEntity::class, parentColumns = ["id"], childColumns = ["accountId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("transactionId"), Index("accountId")]
)
data class LedgerEntryEntity(
    @PrimaryKey val id: String,
    val transactionId: String,
    val accountId: String,
    val deltaMinor: Long
)

data class AccountBalanceRow(val id: String, val name: String, val type: String, val balanceMinor: Long, val archived: Boolean)

@Dao
interface FinanceDao {
    @Query("SELECT * FROM accounts WHERE archived = 0 ORDER BY createdAt") fun observeAccounts(): Flow<List<AccountEntity>>
    @Query("SELECT a.id AS id, a.name AS name, a.type AS type, a.archived AS archived, a.openingBalanceMinor + COALESCE(SUM(l.deltaMinor), 0) AS balanceMinor FROM accounts a LEFT JOIN ledger_entries l ON l.accountId = a.id WHERE a.archived = 0 GROUP BY a.id ORDER BY a.createdAt")
    fun observeBalances(): Flow<List<AccountBalanceRow>>
    @Query("SELECT * FROM transactions ORDER BY occurredAt DESC LIMIT 100") fun observeRecentTransactions(): Flow<List<TransactionEntity>>
    @Query("SELECT * FROM transactions WHERE id = :id") suspend fun transaction(id: String): TransactionEntity?
    @Query("SELECT * FROM transactions WHERE idempotencyKey = :key LIMIT 1") suspend fun transactionByKey(key: String): TransactionEntity?
    @Query("SELECT * FROM accounts WHERE id = :id AND archived = 0") suspend fun activeAccount(id: String): AccountEntity?
    @Query("SELECT COUNT(*) FROM accounts") suspend fun accountCount(): Int
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insertAccount(account: AccountEntity)
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insertTransaction(transaction: TransactionEntity)
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insertEntries(entries: List<LedgerEntryEntity>)
    @Query("DELETE FROM ledger_entries WHERE transactionId = :transactionId") suspend fun deleteEntries(transactionId: String)
    @Query("DELETE FROM transactions WHERE id = :id") suspend fun deleteTransaction(id: String)
    @Update suspend fun updateTransaction(transaction: TransactionEntity)
    @Query("UPDATE accounts SET archived = 1 WHERE id = :id") suspend fun archiveAccount(id: String)
}

@Database(entities = [AccountEntity::class, TransactionEntity::class, LedgerEntryEntity::class], version = 1, exportSchema = true)
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun financeDao(): FinanceDao

    companion object {
        @Volatile private var instance: FinanceDatabase? = null
        fun get(context: Context): FinanceDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, FinanceDatabase::class.java, "hisaabdesk.db")
                .build().also { instance = it }
        }
    }
}
