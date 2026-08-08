package com.example.ui.components

import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {
    fun formatRupee(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        formatter.maximumFractionDigits = 0
        val formatted = formatter.format(amount)
        return if (formatted.startsWith("₹")) formatted else "₹" + formatted.replace("[^0-9,]".toRegex(), "")
    }
}
