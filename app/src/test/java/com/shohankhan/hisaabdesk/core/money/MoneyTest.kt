package com.shohankhan.hisaabdesk.core.money

import com.shohankhan.hisaabdesk.domain.accounting.*
import org.junit.Assert.*
import org.junit.Test

class MoneyTest {
    @Test fun parsesWesternAndBengaliCurrencyWithoutFloatingPoint() {
        assertEquals(10000L, Money.parseBdt("100").poisha)
        assertEquals(12550L, Money.parseBdt("১২৫.৫০").poisha)
        assertEquals(123456789L, Money.parseBdt("1,234,567.89").poisha)
    }

    @Test fun rejectsMalformedPrecisionAndGrouping() {
        listOf("1.001", "1,2,3", "-1", "NaN", "").forEach { value ->
            try { Money.parseBdt(value); fail("Expected rejection for $value") } catch (_: IllegalArgumentException) { }
        }
        try { Money.parseBdt("999999999999999999999999999"); fail("Expected overflow") } catch (_: ArithmeticException) { }
    }

    @Test fun transferPreservesTotalAndCreatesOppositePostings() {
        val postings = LedgerRules.postings(LedgerOperation(OperationKind.TRANSFER, Money(50000), "cash", "bank"))
        assertEquals(-50000L, postings[0].delta.poisha)
        assertEquals(50000L, postings[1].delta.poisha)
        assertEquals(0L, LedgerRules.total(postings).poisha)
        assertEquals(0L, (LedgerRules.balance(Money(100000), postings, "cash") + LedgerRules.balance(Money(25000), postings, "bank")).poisha - 125000L)
    }

    @Test fun incomeAndExpenseHaveExpectedSign() {
        assertEquals(2000L, LedgerRules.postings(LedgerOperation(OperationKind.INCOME, Money(2000), null, "cash")).single().delta.poisha)
        assertEquals(-500L, LedgerRules.postings(LedgerOperation(OperationKind.EXPENSE, Money(500), "cash", null)).single().delta.poisha)
    }

    @Test fun invalidTransferRejectedAndOverflowIsExplicit() {
        try { LedgerRules.postings(LedgerOperation(OperationKind.TRANSFER, Money(100), "cash", "cash")); fail() } catch (_: IllegalArgumentException) { }
        try { Money(Long.MAX_VALUE) + Money(1); fail() } catch (_: ArithmeticException) { }
    }
}
