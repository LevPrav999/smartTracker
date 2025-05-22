package ru.arisubest.smartshopper.presentation.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable

sealed class Screen(val route: String, val icon: @Composable () -> Unit, val label: String) {
    object ShoppingList : Screen("shoppinglist", { Icon(Icons.Filled.List, contentDescription = "List") }, "Покупки")
    object AddItem : Screen("additem", { Icon(Icons.Filled.Add, contentDescription = "Add") }, "Добавить")
    object Summary : Screen("summary", { Icon(Icons.Filled.Info, contentDescription = "Summary") }, "Расчёты")
} 