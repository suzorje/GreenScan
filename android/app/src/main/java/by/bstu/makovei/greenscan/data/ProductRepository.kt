package by.bstu.makovei.greenscan.data

import by.bstu.makovei.greenscan.data.analysis.AllergenDetector
import by.bstu.makovei.greenscan.data.analysis.EnergyRawParser
import by.bstu.makovei.greenscan.data.analysis.Allergen
import by.bstu.makovei.greenscan.data.analysis.EAdditive
import by.bstu.makovei.greenscan.data.analysis.EAdditiveAnalyzer
import by.bstu.makovei.greenscan.data.analysis.GiResult
import by.bstu.makovei.greenscan.data.analysis.GlycemicIndexEstimator
import by.bstu.makovei.greenscan.data.analysis.TrafficLightResult
import by.bstu.makovei.greenscan.data.analysis.TrafficLightScorer
import by.bstu.makovei.greenscan.data.db.AppDatabase
import by.bstu.makovei.greenscan.data.db.CachedOffProductEntity
import by.bstu.makovei.greenscan.data.db.ScanHistoryEntity
import by.bstu.makovei.greenscan.data.off.OffNetworkModule
import by.bstu.makovei.greenscan.data.off.OffProductJson
import com.google.gson.Gson
import com.google.gson.JsonObject

data class ProductDetail(
    val barcode: String,
    val name: String,
    val brand: String?,
    val imageUrl: String?,
    val servingSize: String?,
    val composition: String?,
    val proteinsG: Double?,
    val fatsG: Double?,
    val carbsG: Double?,
    val kcal100: Double?,
    val saturatedFatG: Double?,
    val transFatG: Double?,
    val sugarG: Double?,
    val fiberG: Double?,
    val saltG: Double?,
    val sodiumG: Double?,
    val vitamins: Map<String, Double>,
    val eAdditives: List<EAdditive>,
    val allergens: List<Allergen>,
    val glycemicIndex: GiResult?,
    val trafficLight: TrafficLightResult,
    val priceRub: Double?,
    val greenUrl: String?,
    val category: String?,
    val fromGreen: Boolean,
    val fromOpenFoodFacts: Boolean
)

class ProductRepository(private val db: AppDatabase) {
    private val off = OffNetworkModule.api()
    private val dao = db.productDao()
    private val gson = Gson()

    suspend fun lookup(barcode: String): ProductDetail {
        val clean = barcode.trim().filter { it.isDigit() }
        var name = ""
        var brand: String? = null
        var imageUrl: String? = null
        var servingSize: String? = null
        var composition: String? = null
        var proteins: Double? = null
        var fats: Double? = null
        var carbs: Double? = null
        var kcal: Double? = null
        var saturatedFat: Double? = null
        var transFat: Double? = null
        var sugar: Double? = null
        var fiber: Double? = null
        var salt: Double? = null
        var sodium: Double? = null
        val vitamins = mutableMapOf<String, Double>()
        var price: Double? = null
        var url: String? = null
        var category: String? = null
        var fromGreen = false

        // ── Local Green catalogue ────────────────────────────────────────────
        val pid = dao.findProductIdByBarcode(clean)
        if (pid != null) {
            val p = dao.getProduct(pid)
            val n = dao.getNutrition(pid)
            if (p != null) {
                fromGreen = true
                name = p.name
                composition = p.description
                price = p.priceRub
                url = p.url
                category = p.category
            }
            if (n != null) {
                proteins = n.proteinsG ?: proteins
                fats = n.fatsG ?: fats
                carbs = n.carbohydratesG ?: carbs
                kcal = n.caloriesKcal ?: kcal
                // Parse free-text field when structured columns are missing
                if ((proteins == null || fats == null || carbs == null || kcal == null)
                    && !n.energyRaw.isNullOrBlank()
                ) {
                    val parsed = EnergyRawParser.parse(n.energyRaw)
                    if (proteins == null) proteins = parsed.proteins
                    if (fats == null) fats = parsed.fats
                    if (carbs == null) carbs = parsed.carbs
                    if (kcal == null) kcal = parsed.kcal
                }
                if (composition.isNullOrBlank() && !n.energyRaw.isNullOrBlank()) {
                    composition = n.energyRaw
                }
            }
        }

        // ── Open Food Facts API (with cache fallback) ────────────────────────
        var fromOff = false
        var offProduct: OffProductJson? = null
        try {
            val resp = off.getProduct(clean)
            if (resp.status == 1 && resp.product != null) {
                offProduct = resp.product
                // Cache the raw JSON for offline use
                dao.insertOrReplaceCache(
                    CachedOffProductEntity(
                        barcode = clean,
                        json = gson.toJson(resp.product)
                    )
                )
            }
        } catch (_: Exception) {
            // Offline or API error — try cache
            val cached = dao.getCached(clean)
            if (cached != null) {
                try {
                    offProduct = gson.fromJson(cached.json, OffProductJson::class.java)
                } catch (_: Exception) { /* corrupted cache */ }
            }
        }

        if (offProduct != null) {
            fromOff = true
            val offName = offProduct.productNameRu?.takeIf { it.isNotBlank() }
                ?: offProduct.productName?.takeIf { it.isNotBlank() }
                ?: offProduct.genericName.orEmpty()
            if (name.isBlank() && offName.isNotBlank()) name = offName
            if (brand.isNullOrBlank()) brand = offProduct.brands?.split(",")?.firstOrNull()?.trim()
            if (imageUrl.isNullOrBlank()) imageUrl = offProduct.bestImageUrl()
            if (servingSize.isNullOrBlank()) servingSize = offProduct.servingSize

            val ing = offProduct.ingredientsTextRu?.takeIf { it.isNotBlank() }
                ?: offProduct.ingredientsText
            if (!ing.isNullOrBlank()) composition = ing

            offProduct.nutriments?.let { nut ->
                proteins = nut.doubleField("proteins_100g") ?: proteins
                fats = nut.doubleField("fat_100g") ?: fats
                carbs = nut.doubleField("carbohydrates_100g") ?: carbs
                kcal = nut.doubleField("energy-kcal_100g") ?: kcal
                saturatedFat = nut.doubleField("saturated-fat_100g")
                transFat = nut.doubleField("trans-fat_100g")
                sugar = nut.doubleField("sugars_100g")
                fiber = nut.doubleField("fiber_100g")
                salt = nut.doubleField("salt_100g")
                sodium = nut.doubleField("sodium_100g")

                // Vitamins & minerals
                val vitFields = mapOf(
                    "Витамин A" to "vitamin-a_100g",
                    "Витамин C" to "vitamin-c_100g",
                    "Витамин D" to "vitamin-d_100g",
                    "Витамин E" to "vitamin-e_100g",
                    "Витамин B12" to "vitamin-b12_100g",
                    "Кальций" to "calcium_100g",
                    "Железо" to "iron_100g",
                    "Калий" to "potassium_100g",
                    "Магний" to "magnesium_100g"
                )
                vitFields.forEach { (label, key) ->
                    nut.doubleField(key)?.let { vitamins[label] = it }
                }
            }
        }

        if (name.isBlank()) name = "Товар не найден"

        // ── Analysis ─────────────────────────────────────────────────────────
        val eAdditives = EAdditiveAnalyzer.analyze(composition)
        val detectedAllergens = AllergenDetector.detect(composition)
        val trafficLight = TrafficLightScorer.score(
            fat100g = fats,
            saturatedFat100g = saturatedFat,
            sugar100g = sugar,
            salt100g = salt,
            sodium100g = sodium
        )
        val gi = GlycemicIndexEstimator.estimate(
            carbsPer100g = carbs,
            sugarPer100g = sugar,
            fiberPer100g = fiber,
            categoryHint = category,
            nameHint = name
        )

        // ── Save to scan history ─────────────────────────────────────────────
        dao.insertHistory(
            ScanHistoryEntity(
                barcode = clean,
                name = name,
                brand = brand,
                imageUrl = imageUrl
            )
        )

        return ProductDetail(
            barcode = clean,
            name = name,
            brand = brand,
            imageUrl = imageUrl,
            servingSize = servingSize,
            composition = composition,
            proteinsG = proteins,
            fatsG = fats,
            carbsG = carbs,
            kcal100 = kcal,
            saturatedFatG = saturatedFat,
            transFatG = transFat,
            sugarG = sugar,
            fiberG = fiber,
            saltG = salt,
            sodiumG = sodium,
            vitamins = vitamins,
            eAdditives = eAdditives,
            allergens = detectedAllergens,
            glycemicIndex = gi,
            trafficLight = trafficLight,
            priceRub = price,
            greenUrl = url,
            category = category,
            fromGreen = fromGreen,
            fromOpenFoodFacts = fromOff
        )
    }

    suspend fun getHistory() = dao.getHistory()
    suspend fun updateNote(id: Long, note: String?) = dao.updateNote(id, note)
    suspend fun deleteHistoryEntry(id: Long) = dao.deleteHistoryEntry(id)
}

private fun JsonObject.doubleField(key: String): Double? {
    if (!has(key)) return null
    val el = get(key) ?: return null
    return try {
        when {
            el.isJsonPrimitive && el.asJsonPrimitive.isNumber -> el.asDouble
            el.isJsonPrimitive && el.asJsonPrimitive.isString ->
                el.asString.replace(',', '.').toDoubleOrNull()
            else -> null
        }
    } catch (_: Exception) {
        null
    }
}
