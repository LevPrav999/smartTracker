package ru.arisubest.smartshopper

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.arisubest.smartshopper.ui.theme.SmartShopperTheme
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import ru.arisubest.smartshopper.presentation.screen.AddItemScreen
import ru.arisubest.smartshopper.presentation.screen.ShoppingListScreen
import ru.arisubest.smartshopper.presentation.screen.SummaryScreen
import ru.arisubest.smartshopper.presentation.utils.BottomNavigationBar
import ru.arisubest.smartshopper.presentation.utils.Screen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var balanceState: MutableStateFlow<Float>

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartShopperTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SmartShopperApp(balanceState)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SmartShopperApp(balanceState: MutableStateFlow<Float>) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.ShoppingList.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.ShoppingList.route) {
                ShoppingListScreen()
            }
            composable(Screen.AddItem.route) {
                AddItemScreen(balanceState = balanceState)
            }
            composable(Screen.Summary.route) {
                SummaryScreen(balanceState = balanceState)
            }
        }
    }
}
