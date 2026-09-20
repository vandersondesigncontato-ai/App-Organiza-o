package com.example.ui.screens.journal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.DailyAction

val JournalCategories = listOf("Hábito", "Trabalho", "Saúde", "Estudos", "Pessoal", "Foco")
val JournalMoods = listOf("Produtivo", "Focado", "Tranquilo", "Desafiador", "Cansado")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddDailyActionDialog(
    actionToEdit: DailyAction? = null,
    targetDateStr: String,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        description: String,
        category: String,
        mood: String?,
        isCompleted: Boolean
    ) -> Unit
) {
    var title by remember { mutableStateOf(actionToEdit?.title ?: "") }
    var description by remember { mutableStateOf(actionToEdit?.description ?: "") }
    var selectedCategory by remember { mutableStateOf(actionToEdit?.category ?: JournalCategories.first()) }
    var selectedMood by remember { mutableStateOf(actionToEdit?.mood ?: JournalMoods.first()) }
    var isCompleted by remember { mutableStateOf(actionToEdit?.isCompleted ?: false) }
    var titleError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (actionToEdit == null) "Nova Ação Diária" else "Editar Ação Diária",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (titleError && it.isNotBlank()) titleError = false
                    },
                    label = { Text("O que você realizou ou planeja?") },
                    isError = titleError,
                    supportingText = {
                        if (titleError) Text("Por favor, descreva a ação diária")
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("action_title_input")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Detalhes adicionais / Reflexão") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("action_description_input")
                )

                Text(
                    text = "Categoria",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    JournalCategories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) },
                            modifier = Modifier.testTag("journal_cat_$cat")
                        )
                    }
                }

                Text(
                    text = "Como você se sente / Foco",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    JournalMoods.forEach { mood ->
                        FilterChip(
                            selected = selectedMood == mood,
                            onClick = { selectedMood = mood },
                            label = { Text(mood) },
                            modifier = Modifier.testTag("journal_mood_$mood")
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isCompleted,
                        onCheckedChange = { isCompleted = it },
                        modifier = Modifier.testTag("action_completed_checkbox")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ação já foi concluída",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                    } else {
                        onSave(title, description, selectedCategory, selectedMood, isCompleted)
                    }
                },
                modifier = Modifier.testTag("save_action_button")
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_action_button")
            ) {
                Text("Cancelar")
            }
        }
    )
}
