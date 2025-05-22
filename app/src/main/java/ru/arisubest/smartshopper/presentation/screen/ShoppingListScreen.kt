package ru.arisubest.smartshopper.presentation.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.arisubest.smartshopper.R
import ru.arisubest.smartshopper.data.local.ItemCategory
import ru.arisubest.smartshopper.data.local.ShoppingItem
import ru.arisubest.smartshopper.data.remote.RetrofitClient
import ru.arisubest.smartshopper.presentation.viewmodel.BalanceViewModel
import ru.arisubest.smartshopper.presentation.viewmodel.ShoppingListViewModel
import java.time.DayOfWeek

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    modifier: Modifier = Modifier,
    shoppingListViewModel: ShoppingListViewModel = hiltViewModel(),
    balanceViewModel: BalanceViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val shoppingList by shoppingListViewModel.getAllShoppingList().collectAsState(emptyList())
    val currentBalance by balanceViewModel.balance.collectAsState()
    var showAddShoppingItemDialog by rememberSaveable { mutableStateOf(false) }
    var shoppingItemToEdit: ShoppingItem? by rememberSaveable { mutableStateOf(null) }
    var showBarcodeScanner by remember { mutableStateOf(false) }

    Text(
        text = "Текущий баланс: ₽${currentBalance}",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(16.dp)
    )

    if (showBarcodeScanner) {
        BarcodeScannerScreen(
            onBarcodeScanned = { barcode ->
                showBarcodeScanner = false
                coroutineScope.launch {
                    try {
                        val productResponse = withContext(Dispatchers.IO) {
                            RetrofitClient.productApiService.getProduct(barcode)
                        }
                        val productName = productResponse.asJsonObject
                            .getAsJsonObject("product")
                            .get("product_name")
                            .asString

                        shoppingListViewModel.addToShoppingList(
                            ShoppingItem(
                                id = 0,
                                title = productName,
                                category = ItemCategory.MISC,
                                description = "Scanned barcode: $barcode",
                                estimatedPrice = 0f,
                                status = false
                            )
                        )
                    } catch (e: Exception) {
                        shoppingListViewModel.addToShoppingList(
                            ShoppingItem(
                                id = 0,
                                title = "Barcode: $barcode",
                                category = ItemCategory.MISC,
                                description = "Scanned barcode",
                                estimatedPrice = 0f,
                                status = false
                            )
                        )
                    }
                }
            },
            onNavigateBack = { showBarcodeScanner = false }
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (showAddShoppingItemDialog) {
                AddNewShoppingItemForm(
                    shoppingListViewModel = shoppingListViewModel,
                    onDismiss = { showAddShoppingItemDialog = false },
                    shoppingItemToEdit = shoppingItemToEdit
                )
            }

            if (shoppingList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_items),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = shoppingList,
                        key = { it.id }
                    ) { item ->
                        ShoppingItemCard(
                            shoppingItem = item,
                            onRemoveItem = { shoppingListViewModel.deleteFromShoppingList(item) },
                            onShoppingItemCheckChange = { checkState ->
                                shoppingListViewModel.changeShoppingItemStatus(item, checkState)
                            },
                            onEditItem = { editedShoppingItem ->
                                shoppingItemToEdit = editedShoppingItem
                                showAddShoppingItemDialog = true
                            }
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FloatingActionButton(
                    onClick = { showBarcodeScanner = true },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(Icons.Filled.Send, contentDescription = "Scan barcode")
                }

                FloatingActionButton(
                    onClick = { shoppingListViewModel.deleteAllShoppingItems() },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete all")
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AddNewShoppingItemForm(
    shoppingListViewModel: ShoppingListViewModel,
    onDismiss: () -> Unit = {},
    shoppingItemToEdit: ShoppingItem? = null
) {
    var context = LocalContext.current
    
    Dialog(
        onDismissRequest = onDismiss
    ) {
        var shoppingItemTitle by rememberSaveable {
            mutableStateOf(shoppingItemToEdit?.title?: "")
        }

        var shoppingItemCategory by rememberSaveable {
            mutableStateOf(shoppingItemToEdit?.category?: ItemCategory.FOOD)
        }

        var shoppingItemDescription by rememberSaveable {
            mutableStateOf(shoppingItemToEdit?.description?: "")
        }

        var shoppingItemEstimatedPrice by rememberSaveable {
            mutableStateOf(shoppingItemToEdit?.estimatedPrice?: 0f)
        }

        var shoppingItemStatus by rememberSaveable {
            mutableStateOf(shoppingItemToEdit?.status?: false)
        }

        var shoppingItemDay by rememberSaveable {
            mutableStateOf(shoppingItemToEdit?.dayOfWeek?: DayOfWeek.MONDAY)
        }

        var titleErrorText by rememberSaveable {
            mutableStateOf("")
        }

        var titleInputErrorState by rememberSaveable {
            mutableStateOf(false)
        }

        var categoryExpanded by rememberSaveable { mutableStateOf(false) }
        var dayExpanded by rememberSaveable { mutableStateOf(false) }

        fun validateTitle(text: String) {
            val isBlank = text.isBlank()
            titleErrorText = context.getString(R.string.please_enter_a_title)
            titleInputErrorState = isBlank
        }

        var amountInputErrorState by rememberSaveable {
            mutableStateOf(false)
        }

        var amountErrorText by rememberSaveable {
            mutableStateOf("")
        }

        fun validateAmount(text: String) {
            val allDigit = text.all{char -> char.isDigit()}
            amountErrorText = context.getString(R.string.estimated_price_cannot_be_0)
            if (text == "0") {
                amountInputErrorState = true
            }
        }

        Column(
            Modifier
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.medium
                )
                .padding(10.dp)
        ) {

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = shoppingItemTitle,
                isError = titleInputErrorState,
                onValueChange = {
                    shoppingItemTitle = it
                    validateTitle(shoppingItemTitle)
                },
                label = { Text(text = stringResource(R.string.enter_item_here)) },
                singleLine = true,
                supportingText = {
                    if (titleInputErrorState)
                        Text(
                            text = titleErrorText,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                },
                trailingIcon = {
                    if (titleInputErrorState) {
                        Icon(
                            Icons.Filled.Warning, "error",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )

            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = it }
            ) {
                OutlinedTextField(
                    value = shoppingItemCategory.name,
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
                                shoppingItemCategory = category
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
                    value = shoppingItemDay.name,
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
                                shoppingItemDay = day
                                dayExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = shoppingItemDescription,
                onValueChange = {
                    shoppingItemDescription = it
                },
                label = { Text(text = stringResource(R.string.enter_description_here)) },
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = shoppingItemEstimatedPrice.toString(),
                isError = amountInputErrorState,
                onValueChange = {
                    shoppingItemEstimatedPrice = it.toFloatOrNull() ?: 0f
                    validateAmount(shoppingItemEstimatedPrice.toString())
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number
                ),
                label = { Text(text = stringResource(R.string.enter_estimated_price_here)) },
                singleLine = true,
                supportingText = {
                    if (amountInputErrorState)
                        Text(
                            text = amountErrorText,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                },
                trailingIcon = {
                    if (amountInputErrorState) {
                        Icon(
                            Icons.Filled.Warning, "error",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = shoppingItemStatus, onCheckedChange = {
                    shoppingItemStatus = it
                })
                Text(text = stringResource(R.string.got_it))
            }

            Row {
                Button(
                    onClick = {
                        if (shoppingItemTitle.isEmpty()) {
                            titleErrorText = context.getString(R.string.title_cannot_be_empty)
                            titleInputErrorState = true
                        } else if (shoppingItemEstimatedPrice.toString() == "0") {
                            amountErrorText = context.getString(R.string.estimated_price_cannot_be_0)
                            amountInputErrorState = true
                        } else {
                            if (shoppingItemToEdit == null) {
                                shoppingListViewModel.addToShoppingList(
                                    ShoppingItem(
                                        0,
                                        shoppingItemTitle,
                                        shoppingItemCategory,
                                        shoppingItemDescription,
                                        shoppingItemEstimatedPrice,
                                        shoppingItemStatus,
                                        shoppingItemDay
                                    )
                                )
                            } else {
                                var shoppingItemEdited = shoppingItemToEdit.copy(
                                    title = shoppingItemTitle,
                                    category = shoppingItemCategory,
                                    description = shoppingItemDescription,
                                    estimatedPrice = shoppingItemEstimatedPrice,
                                    status = shoppingItemStatus,
                                    dayOfWeek = shoppingItemDay
                                )
                                shoppingListViewModel.editShoppingItem(shoppingItemEdited)
                            }
                            onDismiss()
                        }
                    })
                {
                    Text(text = stringResource(R.string.save))
                }
            }
        }
    }
}

@Composable
private fun ShoppingItemCard(
    shoppingItem: ShoppingItem,
    onRemoveItem: () -> Unit,
    onShoppingItemCheckChange: (Boolean) -> Unit,
    onEditItem: (ShoppingItem) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = shoppingItem.status,
                    onCheckedChange = onShoppingItemCheckChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.outline
                    )
                )
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                ) {
                    Text(
                        text = shoppingItem.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (shoppingItem.status) TextDecoration.LineThrough else null
                    )
                    
                    Text(
                        text = shoppingItem.category.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            if (expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = shoppingItem.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = "Оценочная цена: ₽${shoppingItem.estimatedPrice}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = { onEditItem(shoppingItem) }) {
                            Icon(
                                Icons.Filled.Build,
                                contentDescription = "Редактировать",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onRemoveItem) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Удалить",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Spinner(
    list: List<ItemCategory>,
    preselected: ItemCategory,
    onSelectionChanged: (myData: ItemCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    var selected by remember { mutableStateOf(preselected) }
    var expanded by remember { mutableStateOf(false) }
    OutlinedCard(
        modifier = modifier.clickable {
            expanded = !expanded
        }
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = selected.toString(),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Icon(Icons.Outlined.ArrowDropDown, null, modifier =
            Modifier.padding(8.dp))
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                list.forEach { listEntry ->
                    DropdownMenuItem(
                        onClick = {
                            selected = listEntry
                            expanded = false
                            onSelectionChanged(selected)
                        },
                        text = {
                            Text(
                                text = listEntry.toString(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.Start)
                            )
                        }
                    )
                }
            }
        }
    }
}