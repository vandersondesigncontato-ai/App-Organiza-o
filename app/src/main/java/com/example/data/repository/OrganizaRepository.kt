package com.example.data.repository

import com.example.data.local.DailyActionDao
import com.example.data.local.FinanceDao
import com.example.data.local.KanbanDao
import com.example.data.local.NoteDao
import com.example.data.model.DailyAction
import com.example.data.model.FinanceTransaction
import com.example.data.model.KanbanTask
import com.example.data.model.Note
import kotlinx.coroutines.flow.Flow

class OrganizaRepository(
    private val noteDao: NoteDao,
    private val kanbanDao: KanbanDao,
    private val dailyActionDao: DailyActionDao,
    private val financeDao: FinanceDao
) {
    // --- NOTES ---
    val allNotes: Flow<List<Note>> = noteDao.getAllNotes()

    suspend fun insertNote(note: Note): Long = noteDao.insertNote(note)
    suspend fun updateNote(note: Note) = noteDao.updateNote(note)
    suspend fun deleteNote(note: Note) = noteDao.deleteNote(note)
    suspend fun deleteNoteById(id: Long) = noteDao.deleteNoteById(id)

    // --- KANBAN ---
    val allKanbanTasks: Flow<List<KanbanTask>> = kanbanDao.getAllTasks()

    fun getKanbanTasksByColumn(columnId: String): Flow<List<KanbanTask>> =
        kanbanDao.getTasksByColumn(columnId)

    suspend fun insertKanbanTask(task: KanbanTask): Long = kanbanDao.insertTask(task)
    suspend fun updateKanbanTask(task: KanbanTask) = kanbanDao.updateTask(task)
    suspend fun updateTaskColumn(taskId: Long, newColumnId: String) =
        kanbanDao.updateTaskColumn(taskId, newColumnId)
    suspend fun deleteKanbanTask(task: KanbanTask) = kanbanDao.deleteTask(task)
    suspend fun deleteKanbanTaskById(id: Long) = kanbanDao.deleteTaskById(id)

    // --- DAILY ACTIONS / JOURNAL ---
    val allDailyActions: Flow<List<DailyAction>> = dailyActionDao.getAllActions()

    fun getActionsByDate(dateStr: String): Flow<List<DailyAction>> =
        dailyActionDao.getActionsByDate(dateStr)

    suspend fun insertDailyAction(action: DailyAction): Long = dailyActionDao.insertAction(action)
    suspend fun updateDailyAction(action: DailyAction) = dailyActionDao.updateAction(action)
    suspend fun toggleActionCompletion(id: Long, completed: Boolean) =
        dailyActionDao.updateCompletion(id, completed)
    suspend fun deleteDailyAction(action: DailyAction) = dailyActionDao.deleteAction(action)
    suspend fun deleteDailyActionById(id: Long) = dailyActionDao.deleteActionById(id)

    // --- FINANCES ---
    val allFinanceTransactions: Flow<List<FinanceTransaction>> = financeDao.getAllTransactions()
    val totalIncome: Flow<Double?> = financeDao.getTotalIncome()
    val totalExpense: Flow<Double?> = financeDao.getTotalExpense()

    suspend fun insertTransaction(transaction: FinanceTransaction): Long =
        financeDao.insertTransaction(transaction)
    suspend fun updateTransaction(transaction: FinanceTransaction) =
        financeDao.updateTransaction(transaction)
    suspend fun deleteTransaction(transaction: FinanceTransaction) =
        financeDao.deleteTransaction(transaction)
    suspend fun deleteTransactionById(id: Long) = financeDao.deleteTransactionById(id)
}
