package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DailyAction
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyActionDao {
    @Query("SELECT * FROM daily_actions ORDER BY dateStr DESC, createdAt DESC")
    fun getAllActions(): Flow<List<DailyAction>>

    @Query("SELECT * FROM daily_actions WHERE dateStr = :dateStr ORDER BY isCompleted ASC, createdAt DESC")
    fun getActionsByDate(dateStr: String): Flow<List<DailyAction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAction(action: DailyAction): Long

    @Update
    suspend fun updateAction(action: DailyAction)

    @Query("UPDATE daily_actions SET isCompleted = :completed WHERE id = :id")
    suspend fun updateCompletion(id: Long, completed: Boolean)

    @Delete
    suspend fun deleteAction(action: DailyAction)

    @Query("DELETE FROM daily_actions WHERE id = :id")
    suspend fun deleteActionById(id: Long)
}
