package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_actions")
data class DailyAction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateStr: String, // format YYYY-MM-DD
    val title: String,
    val description: String = "",
    val category: String = "Geral",
    val isCompleted: Boolean = false,
    val mood: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
