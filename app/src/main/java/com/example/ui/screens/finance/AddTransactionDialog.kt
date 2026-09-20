package com.example.ui.screens.finance

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.TransactionType
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.util.DateUtils

val IncomeCategories = listOf("Salário", "Vendas", "Investimentos", "Freelance", "Presente", "Outros")
val ExpenseCategories = listOf("Alimentação", "Moradia", "Transporte", "Lazer", "Saúde", "Educação", "Contas", "Outros")

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    initialType: TransactionType = TransactionType.INCOME,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        amount: Double,
        type: TransactionType,
        category: String,
        notes: String
    ) -> Unit
) {
    var selectedType by remember { mutableStateOf(initialType) }
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember {
        mutableStateOf(if (initialType == TransactionType.INCOME) IncomeCategories.first() else ExpenseCategories.first())
    }
    var notes by remember { mutableStateOf("") }

    var titleError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    val categories = if (selectedType == TransactionType.INCOME) IncomeCategories else ExpenseCategories

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (selectedType == TransactionType.INCOME) "Registrar Nova Entrada" else "Registrar Nova Retirada",
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
                // Segmented Choice: Entrada vs Retirada
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transaction_type_segmented_row")
                ) {
                    SegmentedButton(
                        selected = selectedType == TransactionType.INCOME,
                        onClick = {
                            selectedType = TransactionType.INCOME
                            if (!IncomeCategories.contains(selectedCategory)) {
                                selectedCategory = IncomeCategories.first()
                            }
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        icon = {
                            Icon(
                                Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = if (selectedType == TransactionType.INCOME) IncomeGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = IncomeGreen.copy(alpha = 0.15f),
                            activeContentColor = IncomeGreen
                        )
                    ) {
                        Text("Entrada (+)", fontWeight = FontWeight.Bold)
                    }

                    SegmentedButton(
                        selected = selectedType == TransactionType.EXPENSE,
                        onClick = {
                            selectedType = TransactionType.EXPENSE
                            if (!ExpenseCategories.contains(selectedCategory)) {
                                selectedCategory = ExpenseCategories.first()
                            }
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        icon = {
                            Icon(
                                Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = if (selectedType == TransactionType.EXPENSE) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = ExpenseRed.copy(alpha = 0.15f),
                            activeContentColor = ExpenseRed
                        )
                    ) {
                        Text("Retirada (-)", fontWeight = FontWeight.Bold)
                    }
                }

                // Amount Input
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        if (amountError && it.isNotBlank()) amountError = false
                    },
                    label = { Text("Valor (R$)") },
                    placeholder = { Text("Ex: 150.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = amountError,
                    supportingText = {
                        if (amountError) Text("Informe um valor numérico válido maior que 0")
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transaction_amount_input")
                )

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (titleError && it.isNotBlank()) titleError = false
                    },
                    label = { Text("Descrição da transação") },
                    placeholder = {
                        Text(if (selectedType == TransactionType.INCOME) "Ex: Salário, Projeto Extra" else "Ex: Mercado, Conta de Energia")
                    },
                    isError = titleError,
                    supportingText = {
                        if (titleError) Text("A descrição é obrigatória")
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transaction_title_input")
                )

                // Category Selection
                Text(
                    text = "Categoria",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) },
                            modifier = Modifier.testTag("finance_cat_$cat")
                        )
                    }
                }

                // Optional Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observações (opcional)") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transaction_notes_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedAmount = amountText.replace(",", ".").toDoubleOrNull()
                    var hasError = false
                    if (title.isBlank()) {
                        titleError = true
                        hasError = true
                    }
                    if (parsedAmount == null || parsedAmount <= 0.0) {
                        amountError = true
                        hasError = true
                    }

                    if (!hasError && parsedAmount != null) {
                        onSave(title, parsedAmount, selectedType, selectedCategory, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedType == TransactionType.INCOME) IncomeGreen else ExpenseRed
                ),
                modifier = Modifier.testTag("save_transaction_button")
            ) {
                Text(
                    text = if (selectedType == TransactionType.INCOME) "Confirmar Entrada" else "Confirmar Retirada",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_transaction_button")
            ) {
                Text("Cancelar")
            }
        }
    )
}
