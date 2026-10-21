package com.shohankhan.hisaabdesk.core.money

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

/** BDT represented as integer poisha. Never use floating-point numbers for accounting. */
@JvmInline
value class Money(val poisha: Long) {
    operator fun plus(other: Money) = Money(Math.addExact(poisha, other.poisha))
    operator fun minus(other: Money) = Money(Math.subtractExact(poisha, other.poisha))

    companion object {
        val ZERO = Money(0)

        /** Accepts Bengali or Western digits and optional correctly grouped commas. */
        fun parseBdt(input: String): Money {
            val normalized = input.trim().map { ch ->
                if (ch in '০'..'৯') ('0'.code + (ch.code - '০'.code)).toChar() else ch
            }.joinToString("")
            val valid = Regex("(?:[0-9]+|[0-9]{1,3}(?:,[0-9]{3})+)(?:\\.[0-9]{1,2})?")
            require(valid.matches(normalized)) { "অঙ্কটি সঠিকভাবে লিখুন (সর্বোচ্চ দুই ঘর দশমিক)।" }
            val decimal = BigDecimal(normalized.replace(",", ""))
                .setScale(2, RoundingMode.UNNECESSARY)
                .movePointRight(2)
            return Money(decimal.longValueExact())
        }
    }
}

fun Money.formatBdt(locale: Locale = Locale("bn", "BD")): String {
    val formatter = NumberFormat.getNumberInstance(locale).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
        isGroupingUsed = true
    }
    return "৳${formatter.format(BigDecimal.valueOf(poisha, 2))}"
}
