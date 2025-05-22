package ru.arisubest.smartshopper.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BalanceViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val sharedPreferences = context.getSharedPreferences("balance_prefs", Context.MODE_PRIVATE)
    
    private val _balance = MutableStateFlow(sharedPreferences.getFloat("balance", 0f))
    val balance: StateFlow<Float> = _balance

    fun addBalance(amount: Float) {
        viewModelScope.launch {
            val newBalance = _balance.value + amount
            Log.d("BalanceViewModel", "Adding $amount to balance. Old: ${_balance.value}, New: $newBalance")
            _balance.value = newBalance
            sharedPreferences.edit().putFloat("balance", newBalance).apply()
        }
    }

    fun subtractBalance(amount: Float) {
        viewModelScope.launch {
            val newBalance = _balance.value - amount
            if (newBalance >= 0) {
                Log.d("BalanceViewModel", "Subtracting $amount from balance. Old: ${_balance.value}, New: $newBalance")
                _balance.value = newBalance
                sharedPreferences.edit().putFloat("balance", newBalance).apply()
            } else {
                Log.d("BalanceViewModel", "Cannot subtract $amount: would result in negative balance")
            }
        }
    }
} 