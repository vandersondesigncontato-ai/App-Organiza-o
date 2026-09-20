package com.example.ui.screens.kanban

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.KanbanColumn
import com.example.data.model.KanbanTask
import com.example.data.model.TaskPriority
import com.example.util.FileUtils
import java.io.File

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditKanbanTaskDialog(
    taskToEdit: KanbanTask? = null,
    initialColumn: KanbanColumn = KanbanColumn.TODO,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        description: String,
        columnId: String,
        priority: String,
        imagePath: String?,
        tags: String
    ) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(taskToEdit?.title ?: "") }
    var description by remember { mutableStateOf(taskToEdit?.description ?: "") }
    var selectedColumn by remember {
        mutableStateOf(taskToEdit?.let { KanbanColumn.fromId(it.columnId) } ?: initialColumn)
    }
    var selectedPriority by remember {
        mutableStateOf(taskToEdit?.let { TaskPriority.fromString(it.priority) } ?: TaskPriority.MEDIUM)
    }
    var imagePath by remember { mutableStateOf(taskToEdit?.imagePath) }
    var tags by remember { mutableStateOf(taskToEdit?.tags ?: "") }
    var titleError by remember { mutableStateOf(false) }

    // Android Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            val savedPath = FileUtils.saveImageToInternalStorage(context, it)
            if (savedPath != null) {
                imagePath = savedPath
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (taskToEdit == null) "Nova Tarefa Kanban" else "Editar Tarefa Kanban",
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
                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (titleError && it.isNotBlank()) titleError = false
                    },
                    label = { Text("Título da tarefa") },
                    isError = titleError,
                    supportingText = {
                        if (titleError) Text("O título é obrigatório")
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("kanban_title_input")
                )

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição / Detalhes") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("kanban_description_input")
                )

                // Column Stage (Funnel Stage)
                Text(
                    text = "Etapa do Funil",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    KanbanColumn.entries.forEach { col ->
                        FilterChip(
                            selected = selectedColumn == col,
                            onClick = { selectedColumn = col },
                            label = { Text(col.title) },
                            modifier = Modifier.testTag("column_chip_${col.id}"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                // Priority
                Text(
                    text = "Prioridade",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TaskPriority.entries.forEach { prio ->
                        val isSelected = selectedPriority == prio
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedPriority = prio },
                            label = { Text(prio.label) },
                            modifier = Modifier.testTag("priority_chip_${prio.name}")
                        )
                    }
                }

                // Tags
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags / Etiquetas (ex: Design, Urgente, Bug)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("kanban_tags_input")
                )

                // Image Attachment Section (Mandated in prompt: "adicionar imagens nessa parte do Kanban")
                Text(
                    text = "Imagem Anexa",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )

                if (imagePath != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(File(imagePath!!))
                                .crossfade(true)
                                .build(),
                            contentDescription = "Imagem da tarefa",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize()
                        )

                        Row(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        ) {
                            IconButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Trocar imagem",
                                    tint = Color.White
                                )
                            }
                            IconButton(
                                onClick = { imagePath = null },
                                modifier = Modifier.size(36.dp).testTag("remove_image_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remover imagem",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pick_image_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Anexar Imagem à Tarefa")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                    } else {
                        onSave(
                            title,
                            description,
                            selectedColumn.id,
                            selectedPriority.name,
                            imagePath,
                            tags
                        )
                    }
                },
                modifier = Modifier.testTag("save_kanban_button")
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_kanban_button")
            ) {
                Text("Cancelar")
            }
        }
    )
}
