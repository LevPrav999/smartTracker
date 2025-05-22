package ru.arisubest.smartshopper.presentation.screen

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.arisubest.smartshopper.data.local.ItemCategory
import ru.arisubest.smartshopper.presentation.viewmodel.ShoppingListViewModel
import java.time.DayOfWeek
import kotlin.math.cos
import kotlin.math.sin

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    shoppingListViewModel: ShoppingListViewModel = hiltViewModel(),
    balanceState: StateFlow<Float>
) {
    var showAddBalanceDialog by remember { mutableStateOf(false) }
    var showSubtractBalanceDialog by remember { mutableStateOf(false) }
    var balanceAmount by remember { mutableStateOf("") }
    val context = LocalContext.current
    
    val balance by balanceState.collectAsState()
    val shoppingList by shoppingListViewModel.getAllShoppingList().collectAsState(initial = emptyList())
    
    val totalExpenses = shoppingList.sumOf { it.estimatedPrice.toDouble() }.toFloat()
    val expensesByCategory = shoppingList.groupBy { it.category }
        .mapValues { (_, items) -> items.sumOf { it.estimatedPrice.toDouble() }.toFloat() }

    val expensesByDay = shoppingList.groupBy { it.dayOfWeek }
        .mapValues { (_, items) -> items.sumOf { it.estimatedPrice.toDouble() }.toFloat() }

    val maxExpense = expensesByDay.values.maxOrNull() ?: 0f

    val categoryColors = mapOf(
        ItemCategory.FOOD to Color(0xFF4CAF50),
        ItemCategory.HEALTH to Color(0xFFE91E63),
        ItemCategory.CLOTHES to Color(0xFF2196F3),
        ItemCategory.ELECTRONICS to Color(0xFFFFC107),
        ItemCategory.CLEANING to Color(0xFF9C27B0),
        ItemCategory.RECREATION to Color(0xFFFF5722),
        ItemCategory.MISC to Color(0xFF607D8B)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Текущий баланс",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "₽${String.format("%.2f", balance)}",
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Общие расходы",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "₽${String.format("%.2f", totalExpenses)}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.error
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = { showAddBalanceDialog = true }) {
                        Text("Добавить")
                    }
                    Button(onClick = { showSubtractBalanceDialog = true }) {
                        Text("Вычесть")
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Расходы по категориям",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(8.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2, size.height / 2)
                        val radius = size.width.coerceAtMost(size.height) * 0.4f
                        var startAngle = 0f

                        expensesByCategory.forEach { (category, amount) ->
                            val sweepAngle = (amount / totalExpenses) * 360f
                            val color = categoryColors[category] ?: Color.Gray
                            
                            drawArc(
                                color = color,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = true,
                                topLeft = Offset(center.x - radius, center.y - radius),
                                size = Size(radius * 2, radius * 2)
                            )

                            val labelAngle = startAngle + (sweepAngle / 2)
                            val labelRadius = radius * 1.2f
                            val labelX = center.x + (labelRadius * cos(Math.toRadians(labelAngle.toDouble()))).toFloat()
                            val labelY = center.y + (labelRadius * sin(Math.toRadians(labelAngle.toDouble()))).toFloat()
                            
                            drawContext.canvas.nativeCanvas.apply {
                                drawText(
                                    "${category.name}\n₽${String.format("%.0f", amount)}",
                                    labelX,
                                    labelY,
                                    android.graphics.Paint().apply {
                                        setColor(android.graphics.Color.BLACK)
                                        textSize = 24f
                                        textAlign = android.graphics.Paint.Align.CENTER
                                    }
                                )
                            }
                            
                            startAngle += sweepAngle
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Расходы по дням недели",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val totalWidth = size.width - 32f
                        val barSpacing = 8f
                        val barWidth = (totalWidth - (barSpacing * 6)) / 7
                        val maxHeight = size.height * 0.7f
                        val startX = 16f

                        DayOfWeek.values().forEachIndexed { index, day ->
                            val expense = expensesByDay[day] ?: 0f
                            val barHeight = if (maxExpense > 0) (expense / maxExpense) * maxHeight else 0f

                            val barX = startX + (index * (barWidth + barSpacing))

                            drawRect(
                                color = Color.DarkGray,
                                topLeft = Offset(barX, size.height - barHeight - 24f),
                                size = Size(barWidth, barHeight)
                            )

                            drawContext.canvas.nativeCanvas.apply {
                                drawText(
                                    day.name.take(3),
                                    barX + (barWidth / 2),
                                    size.height - 5f,
                                    android.graphics.Paint().apply {
                                        setColor(android.graphics.Color.BLACK)
                                        textSize = 24f
                                        textAlign = android.graphics.Paint.Align.CENTER
                                    }
                                )
                            }
                        }

                        val yAxisLabels = 5
                        for (i in 0..yAxisLabels) {
                            val y = size.height - (i * maxHeight / yAxisLabels) - 24f
                            val value = (i * maxExpense / yAxisLabels)
                            
                            drawContext.canvas.nativeCanvas.apply {
                                drawText(
                                    "₽${String.format("%.0f", value)}",
                                    5f,
                                    y + 5f,
                                    android.graphics.Paint().apply {
                                        setColor(android.graphics.Color.BLACK)
                                        textSize = 20f
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddBalanceDialog) {
        AlertDialog(
            onDismissRequest = { showAddBalanceDialog = false },
            title = { Text("Добавить баланс") },
            text = {
                OutlinedTextField(
                    value = balanceAmount,
                    onValueChange = { balanceAmount = it },
                    label = { Text("Сумма") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        balanceAmount.toFloatOrNull()?.let { amount ->
                            val prefs = context.getSharedPreferences("balance_prefs", Context.MODE_PRIVATE)
                            val currentBalance = prefs.getFloat("balance", 0f)
                            val newBalance = currentBalance + amount
                            prefs.edit().putFloat("balance", newBalance).apply()
                            (balanceState as MutableStateFlow).value = newBalance
                        }
                        showAddBalanceDialog = false
                        balanceAmount = ""
                    }
                ) {
                    Text("Добавить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBalanceDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    if (showSubtractBalanceDialog) {
        AlertDialog(
            onDismissRequest = { showSubtractBalanceDialog = false },
            title = { Text("Вычесть баланс") },
            text = {
                OutlinedTextField(
                    value = balanceAmount,
                    onValueChange = { balanceAmount = it },
                    label = { Text("Сумма") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        balanceAmount.toFloatOrNull()?.let { amount ->
                            val prefs = context.getSharedPreferences("balance_prefs", Context.MODE_PRIVATE)
                            val currentBalance = prefs.getFloat("balance", 0f)
                            val newBalance = currentBalance - amount
                            prefs.edit().putFloat("balance", newBalance).apply()
                            (balanceState as MutableStateFlow).value = newBalance
                        }
                        showSubtractBalanceDialog = false
                        balanceAmount = ""
                    }
                ) {
                    Text("Вычесть")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubtractBalanceDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
} 