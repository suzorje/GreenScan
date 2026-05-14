package by.bstu.makovei.greenscan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import by.bstu.makovei.greenscan.ui.HistoryScreen
import by.bstu.makovei.greenscan.ui.HomeScreen
import by.bstu.makovei.greenscan.ui.ResultScreen
import by.bstu.makovei.greenscan.ui.ScanScreen
import by.bstu.makovei.greenscan.ui.SettingsScreen
import by.bstu.makovei.greenscan.ui.theme.GreenScanTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GreenScanTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val nav = rememberNavController()
                    NavHost(navController = nav, startDestination = "home") {
                        composable("home") { HomeScreen(nav) }
                        composable("scan") { ScanScreen(nav) }
                        composable("history") { HistoryScreen(nav) }
                        composable("settings") { SettingsScreen(nav) }
                        composable(
                            route = "result/{barcode}",
                            arguments = listOf(
                                navArgument("barcode") { type = NavType.StringType }
                            )
                        ) { entry ->
                            val code = entry.arguments?.getString("barcode").orEmpty()
                            ResultScreen(nav, code)
                        }
                    }
                }
            }
        }
    }
}
