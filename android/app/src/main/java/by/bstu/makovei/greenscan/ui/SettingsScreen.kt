package by.bstu.makovei.greenscan.ui

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import by.bstu.makovei.greenscan.data.analysis.AllergenDetector

const val PREFS_NAME = "greenscan_prefs"
const val PREF_ALLERGIES = "selected_allergies"

fun loadSelectedAllergies(context: Context): Set<String> {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getStringSet(PREF_ALLERGIES, emptySet()) ?: emptySet()
}

fun saveSelectedAllergies(context: Context, keys: Set<String>) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putStringSet(PREF_ALLERGIES, keys)
        .apply()
}

@Composable
fun SettingsScreen(nav: NavController) {
    val context = LocalContext.current
    val allAllergens = AllergenDetector.ALL_ALLERGENS

    var selected by remember {
        mutableStateOf(loadSelectedAllergies(context))
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("Настройки аллергий", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Отметьте аллергены, которые нужно дополнительно выделять при анализе состава.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        allAllergens.forEach { allergen ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                Checkbox(
                    checked = allergen.key in selected,
                    onCheckedChange = { checked ->
                        selected = if (checked) selected + allergen.key
                        else selected - allergen.key
                    }
                )
                Text(allergen.displayName, style = MaterialTheme.typography.bodyLarge)
            }
        }

        Button(
            onClick = {
                saveSelectedAllergies(context, selected)
                nav.popBackStack()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Сохранить")
        }

        Button(
            onClick = { nav.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Отмена")
        }
    }
}
