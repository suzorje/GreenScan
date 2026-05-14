package by.bstu.makovei.greenscan.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import by.bstu.makovei.greenscan.GreenScanApp
import by.bstu.makovei.greenscan.data.db.ScanHistoryEntity
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(nav: NavController) {
    val app = LocalContext.current.applicationContext as GreenScanApp
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(true) }
    var items by remember { mutableStateOf<List<ScanHistoryEntity>>(emptyList()) }
    var editTarget by remember { mutableStateOf<ScanHistoryEntity?>(null) }
    var refreshTick by remember { mutableStateOf(0) }

    LaunchedEffect(refreshTick) {
        loading = true
        items = withContext(Dispatchers.IO) { app.repository.getHistory() }
        loading = false
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text("История сканирований", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))

        when {
            loading -> CircularProgressIndicator()
            items.isEmpty() -> Text("История пуста. Отсканируйте первый продукт!")
            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(items, key = { it.id }) { entry ->
                    HistoryCard(
                        entry = entry,
                        onOpen = { nav.navigate("result/${entry.barcode}") },
                        onEditNote = { editTarget = entry },
                        onDelete = {
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    app.repository.deleteHistoryEntry(entry.id)
                                }
                                refreshTick++
                            }
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { nav.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Назад") }
    }

    editTarget?.let { target ->
        NoteDialog(
            initial = target.note.orEmpty(),
            onConfirm = { newNote ->
                scope.launch {
                    withContext(Dispatchers.IO) {
                        app.repository.updateNote(target.id, newNote.ifBlank { null })
                    }
                    editTarget = null
                    refreshTick++
                }
            },
            onDismiss = { editTarget = null }
        )
    }
}

@Composable
private fun HistoryCard(
    entry: ScanHistoryEntity,
    onOpen: () -> Unit,
    onEditNote: () -> Unit,
    onDelete: () -> Unit
) {
    var confirmDelete by remember { mutableStateOf(false) }
    val dateStr = remember(entry.scannedAt) {
        SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(entry.scannedAt))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(Modifier.padding(12.dp)) {
            if (!entry.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = entry.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .padding(end = 8.dp)
                )
            }
            Column(Modifier.weight(1f)) {
                Text(entry.name, style = MaterialTheme.typography.titleMedium, maxLines = 2)
                if (!entry.brand.isNullOrBlank()) {
                    Text(entry.brand, style = MaterialTheme.typography.bodySmall)
                }
                Text("Код: ${entry.barcode}", style = MaterialTheme.typography.bodySmall)
                Text(dateStr, style = MaterialTheme.typography.labelSmall)
                if (!entry.note.isNullOrBlank()) {
                    Text(
                        "Заметка: ${entry.note}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Row(
                    Modifier.padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = onEditNote) { Text("Заметка") }
                    TextButton(onClick = { confirmDelete = true }) { Text("Удалить") }
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Удалить запись?") },
            text = { Text("Запись о «${entry.name}» будет удалена из истории.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDelete()
                }) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Отмена") }
            }
        )
    }
}

@Composable
private fun NoteDialog(
    initial: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Заметка о продукте") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Заметка") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }) { Text("Сохранить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}
