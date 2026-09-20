package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType(val label: String) {
    INCOME("Entrada"),
    EXPENSE("Retirada");

    companion object {
        fun fromString(value: String): TransactionType =
            if (value.equals("EXPENSE", ignoreCase = true) || value.equals("RETIRADA", ignoreCase = true)) EXPENSE else INCOME
    }
}

@Entity(tableName = "finance_transactions")
data class FinanceTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: String, // "INCOME" or "EXPENSE"
    val category: String = "Outros",
    val dateStr: String, // format YYYY-MM-DD
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
