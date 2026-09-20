package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.DailyAction
import com.example.data.model.FinanceTransaction
import com.example.data.model.KanbanColumn
import com.example.data.model.KanbanTask
import com.example.data.model.Note
import com.example.data.model.TaskPriority
import com.example.data.model.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Database(
    entities = [
        Note::class,
        KanbanTask::class,
        DailyAction::class,
        FinanceTransaction::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun kanbanDao(): KanbanDao
    abstract fun dailyActionDao(): DailyActionDao
    abstract fun financeDao(): FinanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "organiza_database"
                )
                    .addCallback(DatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Pre-populate with initial welcoming data
                CoroutineScope(Dispatchers.IO).launch {
                    val database = getDatabase(context)
                    populateInitialData(database)
                }
            }

            private suspend fun populateInitialData(db: AppDatabase) {
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                // 1. Initial Note
                db.noteDao().insertNote(
                    Note(
                        title = "Bem-vindo ao Organiza!",
                        content = "Aqui você pode criar notas rápidas, organizar suas ideias por categorias e fixar as mais importantes no topo.",
                        category = "Ideias",
                        colorHex = "#FEF08A",
                        isPinned = true
                    )
                )

                // 2. Initial Kanban Tasks
                db.kanbanDao().insertTask(
                    KanbanTask(
                        title = "Planejar metas da semana",
                        description = "Definir prioridades e prazos para o projeto",
                        columnId = KanbanColumn.TODO.id,
                        priority = TaskPriority.HIGH.name,
                        tags = "Planejamento"
                    )
                )
                db.kanbanDao().insertTask(
                    KanbanTask(
                        title = "Desenvolver nova funcionalidade",
                        description = "Implementar telas e regras de negócio",
                        columnId = KanbanColumn.IN_PROGRESS.id,
                        priority = TaskPriority.URGENT.name,
                        tags = "Desenvolvimento"
                    )
                )
                db.kanbanDao().insertTask(
                    KanbanTask(
                        title = "Revisar relatório mensal",
                        description = "Checar métricas de desempenho e resultados",
                        columnId = KanbanColumn.REVIEW.id,
                        priority = TaskPriority.MEDIUM.name,
                        tags = "Gestão"
                    )
                )
                db.kanbanDao().insertTask(
                    KanbanTask(
                        title = "Configurar ambiente do aplicativo",
                        description = "Tudo pronto e funcionando perfeitamente",
                        columnId = KanbanColumn.DONE.id,
                        priority = TaskPriority.LOW.name,
                        tags = "Setup"
                    )
                )

                // 3. Initial Daily Actions
                db.dailyActionDao().insertAction(
                    DailyAction(
                        dateStr = todayStr,
                        title = "Beber 2L de água",
                        description = "Meta de hidratação e bem-estar",
                        category = "Saúde",
                        isCompleted = true,
                        mood = "Produtivo"
                    )
                )
                db.dailyActionDao().insertAction(
                    DailyAction(
                        dateStr = todayStr,
                        title = "Estudar Kotlin & Jetpack Compose",
                        description = "Praticar Room, Coroutines e animações",
                        category = "Estudos",
                        isCompleted = false,
                        mood = "Focado"
                    )
                )

                // 4. Initial Finances
                db.financeDao().insertTransaction(
                    FinanceTransaction(
                        title = "Salário / Proventos",
                        amount = 4500.00,
                        type = TransactionType.INCOME.name,
                        category = "Salário",
                        dateStr = todayStr,
                        notes = "Recebimento mensal principal"
                    )
                )
                db.financeDao().insertTransaction(
                    FinanceTransaction(
                        title = "Supermercado Semanal",
                        amount = 380.50,
                        type = TransactionType.EXPENSE.name,
                        category = "Alimentação",
                        dateStr = todayStr,
                        notes = "Compras essenciais da semana"
                    )
                )
            }
        }
    }
}
