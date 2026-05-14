package by.bstu.makovei.greenscan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import by.bstu.makovei.greenscan.GreenScanApp
import by.bstu.makovei.greenscan.data.ProductDetail
import by.bstu.makovei.greenscan.data.analysis.DangerLevel
import by.bstu.makovei.greenscan.data.analysis.EAdditive
import by.bstu.makovei.greenscan.data.analysis.GiCategory
import by.bstu.makovei.greenscan.data.analysis.NutrientScore
import by.bstu.makovei.greenscan.data.analysis.TrafficColor
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ResultScreen(nav: NavController, barcode: String) {
    val app = LocalContext.current.applicationContext as GreenScanApp
    val context = LocalContext.current

    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var detail by remember { mutableStateOf<ProductDetail?>(null) }

    LaunchedEffect(barcode) {
        loading = true; error = null; detail = null
        try {
            detail = withContext(Dispatchers.IO) { app.repository.lookup(barcode) }
        } catch (e: Exception) {
            error = e.message ?: "Ошибка загрузки"
        }
        loading = false
    }

    Column(Modifier.fillMaxSize()) {
        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            error != null -> ErrorBlock(error!!, nav)
            else -> detail?.let { d ->
                // ── Header ──────────────────────────────────────────────────
                ProductHeader(d, nav)
                // ── Tabs ────────────────────────────────────────────────────
                var tabIndex by remember { mutableIntStateOf(0) }
                val tabs = listOf("Состав", "Пищевая ценность", "Оценка")
                ScrollableTabRow(selectedTabIndex = tabIndex, edgePadding = 0.dp) {
                    tabs.forEachIndexed { i, title ->
                        Tab(
                            selected = tabIndex == i,
                            onClick = { tabIndex = i },
                            text = { Text(title) }
                        )
                    }
                }
                val selectedAllergens = remember {
                    loadSelectedAllergies(context)
                }
                when (tabIndex) {
                    0 -> CompositionTab(d, selectedAllergens)
                    1 -> NutritionTab(d)
                    2 -> ScoreTab(d)
                }
            }
        }
    }
}

@Composable
private fun ErrorBlock(message: String, nav: NavController) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(message, color = MaterialTheme.colorScheme.error)
        Text("Повторите ввод или попробуйте ещё раз.")
        Button(onClick = { nav.navigate("scan") { launchSingleTop = true } }) {
            Text("Сканировать снова")
        }
        Button(onClick = { nav.popBackStack("home", false) }) { Text("На главную") }
    }
}

@Composable
private fun ProductHeader(d: ProductDetail, nav: NavController) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!d.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = d.imageUrl,
                    contentDescription = "Фото товара",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                Spacer(Modifier.width(12.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(d.name, style = MaterialTheme.typography.titleLarge, maxLines = 3)
                if (!d.brand.isNullOrBlank()) {
                    Text(d.brand, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                val src = buildList {
                    if (d.fromGreen) add("Green")
                    if (d.fromOpenFoodFacts) add("Open Food Facts")
                }.joinToString(", ").ifBlank { "нет данных" }
                Text("Источник: $src", style = MaterialTheme.typography.labelSmall)
            }
        }
        if (d.priceRub != null) {
            Text("Цена Green: ${fmtN(d.priceRub)} руб.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
        }
        Button(
            onClick = { nav.popBackStack() },
            modifier = Modifier.padding(top = 8.dp)
        ) { Text("Назад") }
    }
    HorizontalDivider()
}

@Composable
private fun CompositionTab(d: ProductDetail, userAllergenKeys: Set<String>) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Ингредиенты ────────────────────────────────────────────────────
        SectionTitle("Ингредиенты")
        if (d.composition.isNullOrBlank()) {
            Text("Состав не указан", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Text(d.composition, style = MaterialTheme.typography.bodyMedium)
        }

        // ── Е-добавки ──────────────────────────────────────────────────────
        SectionTitle("Пищевые добавки (Е-коды)")
        if (d.eAdditives.isEmpty()) {
            Text("Е-добавки не обнаружены", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            d.eAdditives.forEach { additive ->
                EAdditiveRow(additive)
            }
        }

        // ── Аллергены ──────────────────────────────────────────────────────
        SectionTitle("Аллергены")
        if (d.allergens.isEmpty()) {
            Text("Известные аллергены не обнаружены", color = Color(0xFF2E7D32))
        } else {
            d.allergens.forEach { allergen ->
                val isPersonal = allergen.key in userAllergenKeys
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isPersonal) Color(0xFFFFCDD2) else Color(0xFFFFF9C4),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(8.dp)
                ) {
                    Text(
                        allergen.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isPersonal) FontWeight.Bold else FontWeight.Normal
                    )
                    if (isPersonal) {
                        Spacer(Modifier.width(8.dp))
                        Text("(ваш аллерген)", style = MaterialTheme.typography.labelSmall, color = Color(0xFFC62828))
                    }
                }
            }
        }
    }
}

@Composable
private fun EAdditiveRow(a: EAdditive) {
    val (bg, fg) = when (a.dangerLevel) {
        DangerLevel.SAFE -> Color(0xFFE8F5E9) to Color(0xFF1B5E20)
        DangerLevel.MODERATE -> Color(0xFFFFF9C4) to Color(0xFFF57F17)
        DangerLevel.DANGEROUS -> Color(0xFFFFEBEE) to Color(0xFFC62828)
        DangerLevel.BANNED -> Color(0xFFB71C1C) to Color.White
    }
    val dangerLabel = when (a.dangerLevel) {
        DangerLevel.SAFE -> "безопасен"
        DangerLevel.MODERATE -> "умеренный риск"
        DangerLevel.DANGEROUS -> "потенциально опасен"
        DangerLevel.BANNED -> "запрещён в ряде стран"
    }
    Row(
        Modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = fg)) { append(a.code) }
                    append(" — ${a.name}")
                },
                style = MaterialTheme.typography.bodyMedium
            )
            Text("${a.purpose} · $dangerLabel", style = MaterialTheme.typography.labelSmall, color = fg)
        }
    }
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun NutritionTab(d: ProductDetail) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SectionTitle("КБЖУ на 100 г")
        if (!d.servingSize.isNullOrBlank()) {
            Text("Порция: ${d.servingSize}", style = MaterialTheme.typography.bodySmall)
        }
        NutrientRow("Калории", fmtN(d.kcal100), "ккал")
        NutrientRow("Белки", fmtN(d.proteinsG), "г")
        NutrientRow("Жиры", fmtN(d.fatsG), "г")
        NutrientRow("— из них насыщенные", fmtN(d.saturatedFatG), "г")
        NutrientRow("— в т.ч. трансжиры", fmtN(d.transFatG), "г")
        NutrientRow("Углеводы", fmtN(d.carbsG), "г")
        NutrientRow("— из них сахар", fmtN(d.sugarG), "г")
        NutrientRow("Пищевые волокна", fmtN(d.fiberG), "г")
        NutrientRow("Соль", fmtN(d.saltG), "г")
        NutrientRow("Натрий", fmtN(d.sodiumG), "г")

        if (d.vitamins.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            SectionTitle("Витамины и минералы")
            d.vitamins.forEach { (label, value) ->
                NutrientRow(label, fmtN(value), "мг")
            }
        }
    }
}

@Composable
private fun NutrientRow(label: String, value: String, unit: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text("$value $unit", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
        HorizontalDivider(thickness = 0.5.dp)
}

@Composable
private fun ScoreTab(d: ProductDetail) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Светофор ──────────────────────────────────────────────────────
        SectionTitle("Оценка «Светофор»")
        Text(
            "Оценка основана на содержании жиров, насыщенных жиров, сахара и соли согласно рекомендациям ВОЗ.",
            style = MaterialTheme.typography.bodySmall
        )

        val tl = d.trafficLight
        OverallBadge(tl.overall)

        listOf(tl.fat, tl.saturatedFat, tl.sugar, tl.salt).forEach { ns ->
            TrafficRow(ns)
        }

        // ── Гликемический индекс ───────────────────────────────────────────
        SectionTitle("Гликемический индекс (оценка)")
        val gi = d.glycemicIndex
        if (gi == null) {
            Text("Недостаточно данных для оценки ГИ.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            val giColor = when (gi.category) {
                GiCategory.LOW -> Color(0xFF2E7D32)
                GiCategory.MEDIUM -> Color(0xFFF57F17)
                GiCategory.HIGH -> Color(0xFFC62828)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    Modifier
                        .size(56.dp)
                        .background(giColor, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("${gi.estimatedGi}", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
                Column {
                    Text(gi.category.label, fontWeight = FontWeight.SemiBold, color = giColor)
                    Text(gi.note, style = MaterialTheme.typography.bodySmall)
                }
            }
            Text(
                "* Оценка ГИ приблизительная — основана на составе продукта и категории.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun OverallBadge(color: TrafficColor) {
    val (bg, label) = when (color) {
        TrafficColor.GREEN -> Color(0xFF2E7D32) to "Зелёный — продукт сбалансирован"
        TrafficColor.YELLOW -> Color(0xFFF9A825) to "Жёлтый — умеренное потребление"
        TrafficColor.RED -> Color(0xFFC62828) to "Красный — ограничьте потребление"
    }
    Box(
        Modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(10.dp))
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun TrafficRow(ns: NutrientScore) {
    val (bg, textColor) = when (ns.color) {
        TrafficColor.GREEN -> Color(0xFFE8F5E9) to Color(0xFF1B5E20)
        TrafficColor.YELLOW -> Color(0xFFFFF9C4) to Color(0xFFF57F17)
        TrafficColor.RED -> Color(0xFFFFEBEE) to Color(0xFFC62828)
    }
    val colorLabel = when (ns.color) {
        TrafficColor.GREEN -> "норма"
        TrafficColor.YELLOW -> "умеренно"
        TrafficColor.RED -> "повышено"
    }
    Row(
        Modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(6.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(ns.label, style = MaterialTheme.typography.bodyMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            val valueStr = if (ns.valuePer100g != null) "${fmtN(ns.valuePer100g)} ${ns.unit}" else "—"
            Text(valueStr, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.width(8.dp))
            Text(colorLabel, style = MaterialTheme.typography.labelSmall, color = textColor, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
}

private fun fmtN(v: Double?) = if (v == null) "—" else String.format("%.1f", v)
