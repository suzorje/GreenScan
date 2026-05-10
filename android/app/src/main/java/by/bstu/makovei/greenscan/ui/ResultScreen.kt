package by.bstu.makovei.greenscan.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import by.bstu.makovei.greenscan.GreenScanApp
import by.bstu.makovei.greenscan.data.ProductDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ResultScreen(
    nav: NavController,
    barcode: String
) {
    val app = LocalContext.current.applicationContext as GreenScanApp
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var detail by remember { mutableStateOf<ProductDetail?>(null) }

    LaunchedEffect(barcode) {
        loading = true
        error = null
        detail = null
        try {
            detail = withContext(Dispatchers.IO) { app.repository.lookup(barcode) }
        } catch (e: Exception) {
            error = e.message ?: "Ошибка загрузки"
        }
        loading = false
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when {
            loading -> CircularProgressIndicator()
            error != null -> {
                Text(error!!, color = MaterialTheme.colorScheme.error)
                Text("Повторите ввод или попробуйте сканирование ещё раз.")
                Button(onClick = { nav.navigate("scan") { launchSingleTop = true } }) {
                    Text("Сканировать снова")
                }
                Button(onClick = { nav.popBackStack("home", inclusive = false) }) {
                    Text("На главную")
                }
            }

            else -> {
                val d = detail
                if (d != null) {
                    Text(d.name, style = MaterialTheme.typography.headlineSmall)
                    Text("Код: ${d.barcode}", style = MaterialTheme.typography.bodyMedium)
                    val src = buildList {
                        if (d.fromGreen) add("Green (локально)")
                        if (d.fromOpenFoodFacts) add("Open Food Facts")
                    }.joinToString(", ").ifBlank { "нет данных" }
                    Text("Источники: $src", style = MaterialTheme.typography.labelMedium)
                    if (d.priceRub != null) {
                        Text("Цена в каталоге Green: ${d.priceRub} руб.")
                    }
                    if (d.category != null) {
                        Text("Категория: ${d.category}")
                    }
                    Text("Состав / описание", style = MaterialTheme.typography.titleMedium)
                    Text(
                        d.composition ?: "—",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text("Пищевая ценность (на 100 г, если указано)", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Белки: ${fmt(d.proteinsG)} г\nЖиры: ${fmt(d.fatsG)} г\nУглеводы: ${fmt(d.carbsG)} г\nКкал: ${fmt(d.kcal100)}"
                    )
                    if (d.greenUrl != null) {
                        Text("URL: ${d.greenUrl}", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Button(onClick = { nav.popBackStack() }) {
                    Text("Назад")
                }
            }
        }
    }
}

private fun fmt(v: Double?) = if (v == null) "—" else String.format("%.1f", v)
