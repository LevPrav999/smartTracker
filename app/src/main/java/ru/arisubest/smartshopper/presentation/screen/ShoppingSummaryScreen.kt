package ru.arisubest.smartshopper.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.arisubest.smartshopper.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingSummaryScreen(
    modifier: Modifier = Modifier,
    numFoodItems: Int,
    numHealthItems: Int,
    numClothesItems: Int,
    numElectronicsItems: Int,
    numCleaningItems: Int,
    numRecreationItems: Int,
    numMiscItems: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.shopping_list_summary),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        val totalItems = numFoodItems + numHealthItems + numClothesItems +
                numElectronicsItems + numCleaningItems + numRecreationItems + numMiscItems

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SummaryCard(
                    title = "Всего товаров",
                    value = totalItems.toString(),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            val categories = listOf(
                R.string.food_items to numFoodItems,
                R.string.health_items to numHealthItems,
                R.string.clothes_items to numClothesItems,
                R.string.electronics_items to numElectronicsItems,
                R.string.cleaning_items to numCleaningItems,
                R.string.recreation_items to numRecreationItems,
                R.string.miscellaneous_items to numMiscItems
            )

            items(categories) { (resId, count) ->
                SummaryCard(
                    title = stringResource(resId),
                    value = count.toString(),
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
