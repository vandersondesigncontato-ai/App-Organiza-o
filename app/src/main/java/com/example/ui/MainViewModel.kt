package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.DailyAction
import com.example.data.model.FinanceTransaction
import com.example.data.model.KanbanColumn
import com.example.data.model.KanbanTask
import com.example.data.model.Note
import com.example.data.model.TransactionType
import com.example.data.repository.OrganizaRepository
import com.example.util.DateUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppDestination(val title: String) {
    NOTES("Notas"),
    KANBAN("Kanban"),
    JOURNAL("Diário"),
    FINANCE("Finanças")
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(
    private val repository: OrganizaRepository
) : ViewModel() {

    // Navigation Tab
    private val _selectedDestination = MutableStateFlow(AppDestination.NOTES)
    val selectedDestination: StateFlow<AppDestination> = _selectedDestination.asStateFlow()

    fun selectDestination(destination: AppDestination) {
        _selectedDestination.value = destination
    }

    // -------------------------------------------------------------
    // NOTES STATE & ACTIONS
    // -------------------------------------------------------------
    val noteSearchQuery = MutableStateFlow("")
    val selectedNoteCategory = MutableStateFlow<String?>(null)

    val notes: StateFlow<List<Note>> = combine(
        repository.allNotes,
        noteSearchQuery,
        selectedNoteCategory
    ) { allNotes, query, category ->
        allNotes.filter { note ->
            val matchesQuery = query.isBlank() ||
                    note.title.contains(query, ignoreCase = true) ||
                    note.content.contains(query, ignoreCase = true)
            val matchesCategory = category == null || note.category.equals(category, ignoreCase = true)
            matchesQuery && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun saveNote(
        id: Long = 0,
        title: String,
        content: String,
        category: String,
        colorHex: String,
        isPinned: Boolean = false
    ) {
        viewModelScope.launch {
            val note = Note(
                id = id,
                title = title.trim(),
                content = content.trim(),
                category = category.trim().ifBlank { "Geral" },
                colorHex = colorHex,
                isPinned = isPinned,
                updatedAt = System.currentTimeMillis()
            )
            if (id == 0L) {
                repository.insertNote(note)
            } else {
                repository.updateNote(note)
            }
        }
    }

    fun toggleNotePinned(note: Note) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isPinned = !note.isPinned, updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    // -------------------------------------------------------------
    // KANBAN STATE & ACTIONS
    // -------------------------------------------------------------
    val kanbanTasks: StateFlow<List<KanbanTask>> = repository.allKanbanTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun saveKanbanTask(
        id: Long = 0,
        title: String,
        description: String,
        columnId: String,
        priority: String,
        imagePath: String?,
        tags: String
    ) {
        viewModelScope.launch {
            val task = KanbanTask(
                id = id,
                title = title.trim(),
                description = description.trim(),
                columnId = columnId,
                priority = priority,
                imagePath = imagePath,
                tags = tags.trim(),
                createdAt = if (id == 0L) System.currentTimeMillis() else System.currentTimeMillis()
            )
            if (id == 0L) {
                repository.insertKanbanTask(task)
            } else {
                repository.updateKanbanTask(task)
            }
        }
    }

    fun moveTaskToColumn(taskId: Long, targetColumn: KanbanColumn) {
        viewModelScope.launch {
            repository.updateTaskColumn(taskId, targetColumn.id)
        }
    }

    fun deleteKanbanTask(task: KanbanTask) {
        viewModelScope.launch {
            repository.deleteKanbanTask(task)
        }
    }

    // -------------------------------------------------------------
    // DAILY JOURNAL / ACTIONS STATE & ACTIONS
    // -------------------------------------------------------------
    val selectedJournalDate = MutableStateFlow(DateUtils.todayIso())

    val dailyActions: StateFlow<List<DailyAction>> = selectedJournalDate
        .flatMapLatest { date ->
            repository.getActionsByDate(date)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val dailyProgress: StateFlow<Float> = dailyActions.map { actions ->
        if (actions.isEmpty()) 0f
        else actions.count { it.isCompleted }.toFloat() / actions.size
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0f
    )

    fun selectJournalDate(dateStr: String) {
        selectedJournalDate.value = dateStr
    }

    fun saveDailyAction(
        id: Long = 0,
        title: String,
        description: String,
        category: String,
        mood: String?,
        isCompleted: Boolean = false,
        dateStr: String = selectedJournalDate.value
    ) {
        viewModelScope.launch {
            val action = DailyAction(
                id = id,
                dateStr = dateStr,
                title = title.trim(),
                description = description.trim(),
                category = category.trim().ifBlank { "Geral" },
                isCompleted = isCompleted,
                mood = mood
            )
            if (id == 0L) {
                repository.insertDailyAction(action)
            } else {
                repository.updateDailyAction(action)
            }
        }
    }

    fun toggleDailyActionCompletion(action: DailyAction) {
        viewModelScope.launch {
            repository.toggleActionCompletion(action.id, !action.isCompleted)
        }
    }

    fun deleteDailyAction(action: DailyAction) {
        viewModelScope.launch {
            repository.deleteDailyAction(action)
        }
    }

    // -------------------------------------------------------------
    // FINANCES STATE & ACTIONS (AUTOMATIC BALANCE CALCULATION)
    // -------------------------------------------------------------
    val financeTransactions: StateFlow<List<FinanceTransaction>> = repository.allFinanceTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val totalIncome: StateFlow<Double> = repository.totalIncome
        .map { it ?: 0.0 }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val totalExpense: StateFlow<Double> = repository.totalExpense
        .map { it ?: 0.0 }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    // Current balance is automatically calculated whenever income or expense changes
    val currentBalance: StateFlow<Double> = combine(
        totalIncome,
        totalExpense
    ) { income, expense ->
        income - expense
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    fun addTransaction(
        title: String,
        amount: Double,
        type: TransactionType,
        category: String,
        notes: String = "",
        dateStr: String = DateUtils.todayIso()
    ) {
        viewModelScope.launch {
            val tx = FinanceTransaction(
                title = title.trim(),
                amount = kotlin.math.abs(amount),
                type = type.name,
                category = category.trim().ifBlank { "Outros" },
                dateStr = dateStr,
                notes = notes.trim()
            )
            repository.insertTransaction(tx)
        }
    }

    fun deleteTransaction(tx: FinanceTransaction) {
        viewModelScope.launch {
            repository.deleteTransaction(tx)
        }
    }

    class Factory(private val repository: OrganizaRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
