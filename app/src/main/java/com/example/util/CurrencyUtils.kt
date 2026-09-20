package com.example.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {
    private val ptBrLocale = Locale("pt", "BR")
    private val formatter = NumberFormat.getCurrencyInstance(ptBrLocale)

    fun format(amount: Double): String {
        return try {
            formatter.format(amount)
        } catch (e: Exception) {
            "R$ %.2f".format(amount)
        }
    }
}
