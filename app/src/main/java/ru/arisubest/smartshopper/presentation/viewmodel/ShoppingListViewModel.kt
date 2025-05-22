package ru.arisubest.smartshopper.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ru.arisubest.smartshopper.data.local.ShoppingItem
import ru.arisubest.smartshopper.data.local.ShoppingItemDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val shoppingItemDao: ShoppingItemDao,
    @ApplicationContext private val context: Context,
    private val balanceState: MutableStateFlow<Float>
) : ViewModel() {

    fun getAllShoppingList(): Flow<List<ShoppingItem>> {
        return shoppingItemDao.getAllShoppingItems()
    }

    fun addToShoppingList(shoppingItem: ShoppingItem) {
        viewModelScope.launch {
            try {
                shoppingItemDao.insertShoppingItem(shoppingItem)
                Log.d("ShoppingListViewModel", "Adding item: ${shoppingItem.title}, price: ${shoppingItem.estimatedPrice}")
                updateBalance(-shoppingItem.estimatedPrice)
            } catch (e: Exception) {
                Log.e("ShoppingListViewModel", "Error adding item", e)
            }
        }
    }

    fun deleteFromShoppingList(shoppingItem: ShoppingItem) {
        viewModelScope.launch {
            try {
                shoppingItemDao.deleteShoppingItem(shoppingItem)
                Log.d("ShoppingListViewModel", "Deleting item: ${shoppingItem.title}, price: ${shoppingItem.estimatedPrice}")
                updateBalance(shoppingItem.estimatedPrice)
            } catch (e: Exception) {
                Log.e("ShoppingListViewModel", "Error deleting item", e)
            }
        }
    }

    fun changeShoppingItemStatus(shoppingItem: ShoppingItem, status: Boolean) {
        viewModelScope.launch {
            val updatedItem = shoppingItem.copy(status = status)
            shoppingItemDao.updateShoppingItem(updatedItem)
        }
    }

    fun editShoppingItem(shoppingItem: ShoppingItem) {
        viewModelScope.launch {
            try {
                val oldItems = shoppingItemDao.getAllShoppingItems().first()
                val oldItem = oldItems.find { it.id == shoppingItem.id }
                
                shoppingItemDao.updateShoppingItem(shoppingItem)

                if (oldItem != null) {
                    val priceDifference = oldItem.estimatedPrice - shoppingItem.estimatedPrice
                    Log.d("ShoppingListViewModel", "Editing item: ${shoppingItem.title}, price difference: $priceDifference")
                    updateBalance(priceDifference)
                }
            } catch (e: Exception) {
                Log.e("ShoppingListViewModel", "Error editing item", e)
            }
        }
    }

    fun deleteAllShoppingItems() {
        viewModelScope.launch {
            try {
                val items = shoppingItemDao.getAllShoppingItems().first()
                val totalRefund = items.sumOf { it.estimatedPrice.toDouble() }.toFloat()
                
                shoppingItemDao.deleteAllShoppingItems()

                if (totalRefund > 0) {
                    Log.d("ShoppingListViewModel", "Deleting all items, total refund: $totalRefund")
                    updateBalance(totalRefund)
                }
            } catch (e: Exception) {
                Log.e("ShoppingListViewModel", "Error deleting all items", e)
            }
        }
    }

    private fun updateBalance(amount: Float) {
        val prefs = context.getSharedPreferences("balance_prefs", Context.MODE_PRIVATE)
        val currentBalance = prefs.getFloat("balance", 0f)
        val newBalance = currentBalance + amount
        prefs.edit().putFloat("balance", newBalance).apply()
        balanceState.value = newBalance
    }
} 