package by.bstu.makovei.greenscan.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun HomeScreen(nav: NavController) {
    val code = remember { mutableStateOf("") }
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("GreenScan", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Анализ состава продуктов по штрих-коду",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(4.dp))

        OutlinedTextField(
            value = code.value,
            onValueChange = { code.value = it.filter { ch -> ch.isDigit() } },
            label = { Text("Штрихкод (EAN-13 и др.)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Button(
            onClick = {
                if (code.value.isNotBlank()) nav.navigate("result/${code.value}")
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = code.value.length >= 8
        ) {
            Text("Найти по коду")
        }
        Button(
            onClick = { nav.navigate("scan") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Сканировать камерой")
        }

        HorizontalDivider(Modifier.padding(vertical = 4.dp))

        OutlinedButton(
            onClick = { nav.navigate("history") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("История сканирований")
        }
        OutlinedButton(
            onClick = { nav.navigate("settings") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Настройки аллергий")
        }

        Spacer(Modifier.height(8.dp))
        Text(
            "Данные: локальная база Green + Open Food Facts.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
