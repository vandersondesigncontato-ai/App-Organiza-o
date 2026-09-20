package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val ptBrLocale = Locale("pt", "BR")
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displayDateFormat = SimpleDateFormat("dd 'de' MMMM", ptBrLocale)
    private val fullDateFormat = SimpleDateFormat("dd/MM/yyyy", ptBrLocale)

    fun todayIso(): String = isoFormat.format(Date())

    fun formatDisplayDate(isoDate: String): String {
        return try {
            val date = isoFormat.parse(isoDate) ?: return isoDate
            val todayStr = todayIso()

            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val yesterdayStr = isoFormat.format(cal.time)

            when (isoDate) {
                todayStr -> "Hoje, ${displayDateFormat.format(date)}"
                yesterdayStr -> "Ontem, ${displayDateFormat.format(date)}"
                else -> displayDateFormat.format(date)
            }
        } catch (e: Exception) {
            isoDate
        }
    }

    fun formatShortDate(timestamp: Long): String {
        return fullDateFormat.format(Date(timestamp))
    }
}
