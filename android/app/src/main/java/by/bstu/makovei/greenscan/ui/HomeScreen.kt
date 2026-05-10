package by.bstu.makovei.greenscan.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Введите штрихкод (EAN-13 и др.) или отсканируйте.")
        OutlinedTextField(
            value = code.value,
            onValueChange = { code.value = it.filter { ch -> ch.isDigit() } },
            label = { Text("Штрихкод") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Button(
            onClick = {
                if (code.value.isNotBlank()) {
                    nav.navigate("result/${code.value}")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = code.value.length >= 8
        ) {
            Text("Найти")
        }
        Button(
            onClick = { nav.navigate("scan") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Сканировать камерой")
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Данные: локальная база Green + Open Food Facts (при наличии сети).",
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall
        )
    }
}
