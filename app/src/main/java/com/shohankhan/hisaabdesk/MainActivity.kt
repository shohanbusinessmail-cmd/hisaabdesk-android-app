package com.shohankhan.hisaabdesk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.shohankhan.hisaabdesk.data.local.FinanceDatabase
import com.shohankhan.hisaabdesk.data.repository.FinanceRepository
import com.shohankhan.hisaabdesk.ui.HisaabDeskApp
import com.shohankhan.hisaabdesk.ui.theme.HisaabDeskTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = FinanceRepository(FinanceDatabase.get(this))
        lifecycleScope.launch { runCatching { repository.ensureInitialCashAccount() } }
        setContent { HisaabDeskTheme { HisaabDeskApp(repository) } }
    }
}
