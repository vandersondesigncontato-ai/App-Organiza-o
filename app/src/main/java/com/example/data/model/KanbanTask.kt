package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class KanbanColumn(val id: String, val title: String) {
    TODO("TODO", "A Fazer"),
    IN_PROGRESS("IN_PROGRESS", "Em Progresso"),
    REVIEW("REVIEW", "Revisão"),
    DONE("DONE", "Concluído");

    companion object {
        fun fromId(id: String): KanbanColumn = entries.find { it.id == id } ?: TODO
    }
}

enum class TaskPriority(val label: String, val colorHex: String) {
    LOW("Baixa", "#10B981"),
    MEDIUM("Média", "#3B82F6"),
    HIGH("Alta", "#F59E0B"),
    URGENT("Urgente", "#EF4444");

    companion object {
        fun fromString(str: String): TaskPriority =
            entries.find { it.name.equals(str, ignoreCase = true) || it.label.equals(str, ignoreCase = true) } ?: MEDIUM
    }
}

@Entity(tableName = "kanban_tasks")
data class KanbanTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val columnId: String = KanbanColumn.TODO.id,
    val priority: String = TaskPriority.MEDIUM.name,
    val imagePath: String? = null,
    val tags: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
