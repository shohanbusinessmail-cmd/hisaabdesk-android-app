package com.shohankhan.hisaabdesk.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shohankhan.hisaabdesk.core.money.Money
import com.shohankhan.hisaabdesk.core.money.formatBdt
import com.shohankhan.hisaabdesk.data.local.AccountBalanceRow
import com.shohankhan.hisaabdesk.data.local.TransactionEntity
import com.shohankhan.hisaabdesk.data.repository.FinanceRepository
import com.shohankhan.hisaabdesk.domain.accounting.OperationKind
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

@Composable
fun HisaabDeskApp(repository: FinanceRepository) {
    val accounts by repository.balances.collectAsStateWithLifecycle(initialValue = emptyList())
    val transactions by repository.recentTransactions.collectAsStateWithLifecycle(initialValue = emptyList())
    var showTransaction by remember { mutableStateOf(false) }
    var showAccount by remember { mutableStateOf(false) }
    var selectedTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var confirmDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let { snackbar.showSnackbar(it); message = null }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showTransaction = true }, containerColor = MaterialTheme.colorScheme.primary) {
                Text("＋", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.headlineSmall)
            }
        }
    ) { insets ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(insets).imePadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text("হিসাবডেস্ক", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("আপনার হিসাব, আপনার ডিভাইসেই", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            item {
                Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Column(Modifier.fillMaxWidth().padding(20.dp)) {
                        Text("সব সক্রিয় হিসাবে মোট", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .85f), style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(Money(sumBalances(accounts)).formatBdt(), color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text("এটি হিসাবের সম্পদ-স্থিতি; দেনা/ঋণ এখনো এখানে সমন্বিত নয়।", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .82f), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("আমার হিসাব", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    TextButton(onClick = { showAccount = true }) { Text("＋ হিসাব") }
                }
            }
            if (accounts.isEmpty()) item { EmptyCard("কোনো হিসাব নেই", "নতুন হিসাব যোগ করে শুরু করুন।") }
            items(accounts, key = { it.id }) { account -> AccountCard(account) }
            item { Text("সাম্প্রতিক লেনদেন", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold) }
            if (transactions.isEmpty()) item { EmptyCard("এখনো কোনো লেনদেন নেই", "＋ চাপ দিয়ে আয়, ব্যয় বা স্থানান্তর যোগ করুন।") }
            items(transactions, key = { it.id }) { transaction -> TransactionCard(transaction) { selectedTransaction = transaction } }
        }
    }

    if (showTransaction && accounts.isNotEmpty()) {
        TransactionDialog(
            accounts = accounts,
            transaction = editingTransaction,
            onDismiss = { showTransaction = false; editingTransaction = null },
            onSave = { kind, amount, from, to, category, note ->
                scope.launch {
                    runCatching {
                        val old = editingTransaction
                        if (old == null) repository.createTransaction(kind, amount, from, to, category, note)
                        else repository.editTransaction(old.id, kind, amount, from, to, category, note, old.occurredAt)
                    }
                        .onSuccess { message = if (editingTransaction == null) "লেনদেন সংরক্ষণ হয়েছে" else "লেনদেন সংশোধন হয়েছে" }
                        .onFailure { message = it.message ?: "সংরক্ষণ করা যায়নি" }
                    showTransaction = false
                    editingTransaction = null
                }
            }
        )
    }
    selectedTransaction?.let { transaction ->
        TransactionDetailsDialog(
            transaction = transaction,
            onDismiss = { selectedTransaction = null },
            onEdit = { editingTransaction = transaction; selectedTransaction = null; showTransaction = true },
            onDelete = { confirmDelete = transaction; selectedTransaction = null }
        )
    }
    confirmDelete?.let { transaction ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            title = { Text("লেনদেন মুছবেন?") },
            text = { Text("${Money(transaction.amountMinor).formatBdt()} লেনদেন এবং সংশ্লিষ্ট হিসাবের পোস্টিং মুছে যাবে।") },
            confirmButton = { TextButton(onClick = {
                scope.launch {
                    runCatching { repository.deleteTransaction(transaction.id) }
                        .onSuccess { message = "লেনদেন মুছে ফেলা হয়েছে" }
                        .onFailure { message = it.message ?: "মুছতে পারিনি" }
                    confirmDelete = null
                }
            }) { Text("মুছুন") } },
            dismissButton = { TextButton(onClick = { confirmDelete = null }) { Text("বাতিল") } }
        )
    }
    if (showAccount) {
        AccountDialog(onDismiss = { showAccount = false }, onSave = { name ->
            scope.launch {
                runCatching { repository.createAccount(name) }
                    .onSuccess { message = "হিসাব যোগ হয়েছে" }
                    .onFailure { message = it.message ?: "হিসাব যোগ করা যায়নি" }
                showAccount = false
            }
        })
    }
}

private fun sumBalances(accounts: List<AccountBalanceRow>): Long = accounts.fold(0L) { sum, row -> Math.addExact(sum, row.balanceMinor) }

@Composable
private fun AccountCard(account: AccountBalanceRow) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column { Text(account.name, fontWeight = FontWeight.SemiBold); Text(account.typeLabel(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Text(Money(account.balanceMinor).formatBdt(), fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun TransactionCard(transaction: TransactionEntity, onClick: () -> Unit) {
    val title = when (transaction.kind) { "INCOME" -> "আয়"; "EXPENSE" -> "ব্যয়"; "TRANSFER" -> "হিসাব স্থানান্তর"; else -> "লেনদেন" }
    val sign = if (transaction.kind == "INCOME") "+" else if (transaction.kind == "EXPENSE") "−" else ""
    Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.clickable(onClick = onClick)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(transaction.category?.takeIf { it.isNotBlank() } ?: title, fontWeight = FontWeight.Medium)
                Text(transaction.note?.takeIf { it.isNotBlank() } ?: DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(transaction.occurredAt)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("$sign${Money(transaction.amountMinor).formatBdt()}", color = if (transaction.kind == "EXPENSE") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun EmptyCard(title: String, detail: String) {
    Card(shape = RoundedCornerShape(16.dp)) { Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(title, fontWeight = FontWeight.SemiBold); Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
}

private fun AccountBalanceRow.typeLabel(): String = when (type) { "CASH" -> "নগদ"; "BANK" -> "ব্যাংক"; "MFS" -> "মোবাইল আর্থিক সেবা"; "SAVINGS" -> "সঞ্চয়"; else -> "অন্যান্য" }

@Composable
private fun AccountDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("নতুন হিসাব") }, text = {
        Column { OutlinedTextField(name, { name = it; error = null }, label = { Text("হিসাবের নাম") }, singleLine = true); error?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
    }, confirmButton = { TextButton(onClick = { if (name.isBlank()) error = "হিসাবের নাম লিখুন" else onSave(name.trim()) }) { Text("যোগ করুন") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("বাতিল") } })
}

@Composable
private fun TransactionDialog(
    accounts: List<AccountBalanceRow>, transaction: TransactionEntity?, onDismiss: () -> Unit,
    onSave: (OperationKind, Money, String?, String?, String?, String?) -> Unit
) {
    var kind by remember(transaction?.id) { mutableStateOf(runCatching { OperationKind.valueOf(transaction?.kind ?: "EXPENSE") }.getOrDefault(OperationKind.EXPENSE)) }
    var amountText by remember(transaction?.id) { mutableStateOf(transaction?.amountMinor?.let { minor -> "${minor / 100}.${(minor % 100).toString().padStart(2, '0')}" } ?: "") }
    var category by remember(transaction?.id) { mutableStateOf(transaction?.category.orEmpty()) }
    var note by remember(transaction?.id) { mutableStateOf(transaction?.note.orEmpty()) }
    var from by remember(transaction?.id) { mutableStateOf(transaction?.sourceAccountId ?: accounts.firstOrNull()?.id) }
    var to by remember(transaction?.id) { mutableStateOf(transaction?.destinationAccountId ?: accounts.getOrNull(1)?.id ?: accounts.firstOrNull()?.id) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("নতুন লেনদেন") },
        text = {
            Column(Modifier.heightIn(max = 480.dp).imePadding().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(OperationKind.INCOME to "আয়", OperationKind.EXPENSE to "ব্যয়", OperationKind.TRANSFER to "স্থানান্তর").forEach { (value, label) ->
                        FilterChip(selected = kind == value, onClick = { kind = value; error = null }, label = { Text(label) })
                    }
                }
                OutlinedTextField(amountText, { amountText = it; error = null }, label = { Text("পরিমাণ (৳)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                if (kind == OperationKind.TRANSFER) {
                    AccountSelector("যে হিসাব থেকে", accounts, from, { from = it }, "হিসাব বাছুন")
                    AccountSelector("যে হিসাবে", accounts, to, { to = it }, "হিসাব বাছুন")
                } else {
                    AccountSelector(if (kind == OperationKind.INCOME) "যে হিসাবে জমা" else "যে হিসাব থেকে পরিশোধ", accounts, if (kind == OperationKind.INCOME) to else from, { selected -> if (kind == OperationKind.INCOME) to = selected else from = selected }, "হিসাব বাছুন")
                    if (kind != OperationKind.TRANSFER) OutlinedTextField(category, { category = it }, label = { Text("বিভাগ (ঐচ্ছিক)") }, singleLine = true)
                }
                OutlinedTextField(note, { note = it }, label = { Text("বিবরণ (ঐচ্ছিক)") }, minLines = 1, maxLines = 3)
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = { TextButton(onClick = {
            try {
                val parsed = Money.parseBdt(amountText)
                if (kind == OperationKind.TRANSFER && from == to) throw IllegalArgumentException("আলাদা দুটি হিসাব বাছুন।")
                onSave(kind, parsed, if (kind == OperationKind.INCOME) null else from, if (kind == OperationKind.EXPENSE) null else to, category, note)
            } catch (e: Exception) { error = e.message ?: "তথ্য যাচাই করুন" }
        }) { Text("সংরক্ষণ") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("বাতিল") } }
    )
}

@Composable
private fun TransactionDetailsDialog(
    transaction: TransactionEntity,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val typeLabel = when (transaction.kind) {
        "INCOME" -> "আয়"
        "EXPENSE" -> "ব্যয়"
        "TRANSFER" -> "হিসাব স্থানান্তর"
        else -> "লেনদেন"
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(transaction.category?.takeIf(String::isNotBlank) ?: typeLabel) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(Money(transaction.amountMinor).formatBdt(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(transaction.occurredAt)))
                transaction.note?.takeIf(String::isNotBlank)?.let { Text(it) }
            }
        },
        confirmButton = { TextButton(onClick = onEdit) { Text("সম্পাদনা") } },
        dismissButton = {
            Row {
                TextButton(onClick = onDelete) { Text("মুছুন", color = MaterialTheme.colorScheme.error) }
                TextButton(onClick = onDismiss) { Text("বন্ধ") }
            }
        }
    )
}

@Composable
private fun AccountSelector(label: String, accounts: List<AccountBalanceRow>, selected: String?, onSelect: (String) -> Unit, placeholder: String) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text("$label: ${accounts.firstOrNull { it.id == selected }?.name ?: placeholder}")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            accounts.forEach { account -> DropdownMenuItem(text = { Text(account.name) }, onClick = { onSelect(account.id); expanded = false }) }
        }
    }
}

