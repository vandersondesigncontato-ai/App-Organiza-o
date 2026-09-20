package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.repository.OrganizaRepository
import com.example.ui.AppDestination
import com.example.ui.MainViewModel
import com.example.ui.components.OrganizaBottomNav
import com.example.ui.screens.finance.FinanceScreen
import com.example.ui.screens.journal.DailyJournalScreen
import com.example.ui.screens.kanban.KanbanScreen
import com.example.ui.screens.notes.NotesScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                OrganizaApp()
            }
        }
    }
}

@Composable
fun OrganizaApp() {
    val context = LocalContext.current
    val database = rememberDatabase(context)
    val repository = OrganizaRepository(
        noteDao = database.noteDao(),
        kanbanDao = database.kanbanDao(),
        dailyActionDao = database.dailyActionDao(),
        financeDao = database.financeDao()
    )
    val viewModel: MainViewModel = viewModel(factory = MainViewModel.Factory(repository))
    val currentDestination by viewModel.selectedDestination.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            OrganizaBottomNav(
                currentDestination = currentDestination,
                onDestinationSelected = { viewModel.selectDestination(it) }
            )
        }
    ) { innerPadding ->
        Crossfade(
            targetState = currentDestination,
            label = "ScreenTransition",
            modifier = Modifier.padding(innerPadding)
        ) { destination ->
            when (destination) {
                AppDestination.NOTES -> NotesScreen(viewModel = viewModel)
                AppDestination.KANBAN -> KanbanScreen(viewModel = viewModel)
                AppDestination.JOURNAL -> DailyJournalScreen(viewModel = viewModel)
                AppDestination.FINANCE -> FinanceScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun rememberDatabase(context: android.content.Context): AppDatabase {
    return androidx.compose.runtime.remember {
        AppDatabase.getDatabase(context)
    }
}

// Retained for backward-compatibility with template unit & screenshot tests
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        Greeting("Organiza")
    }
}
