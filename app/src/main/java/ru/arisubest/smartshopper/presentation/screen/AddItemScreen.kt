package ru.arisubest.smartshopper.presentation.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import ru.arisubest.smartshopper.R
import ru.arisubest.smartshopper.data.local.ItemCategory
import ru.arisubest.smartshopper.data.local.ShoppingItem
import ru.arisubest.smartshopper.presentation.viewmodel.ShoppingListViewModel
import java.time.DayOfWeek

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen(
    shoppingListViewModel: ShoppingListViewModel = hiltViewModel(),
    balanceState: MutableStateFlow<Float>
) {
    var context = LocalContext.current
    var shoppingItemTitle by remember { mutableStateOf("") }
    var shoppingItemDescription by remember { mutableStateOf("") }
    var shoppingItemEstimatedPrice by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ItemCategory.MISC) }
    var selectedDay by remember { mutableStateOf(DayOfWeek.MONDAY) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var dayExpanded by remember { mutableStateOf(false) }

    var titleErrorText by remember { mutableStateOf("") }
    var titleInputErrorState by remember { mutableStateOf(false) }
    var amountInputErrorState by remember { mutableStateOf(false) }
    var amountErrorText by remember { mutableStateOf("") }

    val balance by balanceState.collectAsState()

    fun validateTitle(text: String) {
        val isBlank = text.isBlank()
        titleErrorText = context.getString(R.string.please_enter_a_title)
        titleInputErrorState = isBlank
    }

    fun validateAmount(text: String) {
        val allDigit = text.all { char -> char.isDigit() }
        amountErrorText = context.getString(R.string.estimated_price_cannot_be_0)
        if (text == "0") {
            amountInputErrorState = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Текущий баланс: ₽${String.format("%.2f", balance)}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        OutlinedTextField(
            value = shoppingItemTitle,
            onValueChange = {
                shoppingItemTitle = it
                validateTitle(shoppingItemTitle)
            },
            label = { Text("Название товара") },
            isError = titleInputErrorState,
            supportingText = {
                if (titleInputErrorState)
                    Text(
                        text = titleErrorText,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = shoppingItemDescription,
            onValueChange = { shoppingItemDescription = it },
            label = { Text("Описание") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = shoppingItemEstimatedPrice,
            onValueChange = {
                shoppingItemEstimatedPrice = it
                validateAmount(shoppingItemEstimatedPrice)
            },
            label = { Text("Оценочная цена") },
            isError = amountInputErrorState,
            supportingText = {
                if (amountInputErrorState)
                    Text(
                        text = amountErrorText,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
            },
            modifier = Modifier.fillMaxWidth()
        )

        ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedCategory.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Категория") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = categoryExpanded,
                onDismissRequest = { categoryExpanded = false }
            ) {
                ItemCategory.values().forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.name) },
                        onClick = {
                            selectedCategory = category
                            categoryExpanded = false
                        }
                    )
                }
            }
        }

        ExposedDropdownMenuBox(
            expanded = dayExpanded,
            onExpandedChange = { dayExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedDay.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Day of Week") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dayExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = dayExpanded,
                onDismissRequest = { dayExpanded = false }
            ) {
                DayOfWeek.values().forEach { day ->
                    DropdownMenuItem(
                        text = { Text(day.name) },
                        onClick = {
                            selectedDay = day
                            dayExpanded = false
                        }
                    )
                }
            }
        }

        Button(
            onClick = {
                if (shoppingItemTitle.isEmpty()) {
                    titleErrorText = context.getString(R.string.title_cannot_be_empty)
                    titleInputErrorState = true
                } else if (shoppingItemEstimatedPrice == "0") {
                    amountErrorText = context.getString(R.string.estimated_price_cannot_be_0)
                    amountInputErrorState = true
                } else {
                    val price = shoppingItemEstimatedPrice.toFloatOrNull() ?: 0f
                    val newItem = ShoppingItem(
                        id = 0,
                        title = shoppingItemTitle,
                        category = selectedCategory,
                        description = shoppingItemDescription,
                        estimatedPrice = price,
                        status = false,
                        dayOfWeek = selectedDay
                    )
                    shoppingListViewModel.addToShoppingList(newItem)

                    shoppingItemTitle = ""
                    shoppingItemDescription = ""
                    shoppingItemEstimatedPrice = ""
                    selectedCategory = ItemCategory.MISC
                    selectedDay = DayOfWeek.MONDAY
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Добавить товар")
        }
    }
} 