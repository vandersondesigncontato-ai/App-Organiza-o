package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.KanbanTask
import kotlinx.coroutines.flow.Flow

@Dao
interface KanbanDao {
    @Query("SELECT * FROM kanban_tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<KanbanTask>>

    @Query("SELECT * FROM kanban_tasks WHERE columnId = :columnId ORDER BY createdAt DESC")
    fun getTasksByColumn(columnId: String): Flow<List<KanbanTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: KanbanTask): Long

    @Update
    suspend fun updateTask(task: KanbanTask)

    @Query("UPDATE kanban_tasks SET columnId = :newColumnId WHERE id = :taskId")
    suspend fun updateTaskColumn(taskId: Long, newColumnId: String)

    @Delete
    suspend fun deleteTask(task: KanbanTask)

    @Query("DELETE FROM kanban_tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Long)
}
