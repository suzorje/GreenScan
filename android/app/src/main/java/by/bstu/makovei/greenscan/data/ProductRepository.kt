package by.bstu.makovei.greenscan.data

import by.bstu.makovei.greenscan.data.db.AppDatabase
import by.bstu.makovei.greenscan.data.off.OffNetworkModule
import com.google.gson.JsonObject

data class ProductDetail(
    val barcode: String,
    val name: String,
    val composition: String?,
    val proteinsG: Double?,
    val fatsG: Double?,
    val carbsG: Double?,
    val kcal100: Double?,
    val priceRub: Double?,
    val greenUrl: String?,
    val category: String?,
    val fromGreen: Boolean,
    val fromOpenFoodFacts: Boolean
)

class ProductRepository(private val db: AppDatabase) {
    private val off = OffNetworkModule.api()
    private val dao = db.productDao()

    suspend fun lookup(barcode: String): ProductDetail {
        val clean = barcode.trim().filter { it.isDigit() }
        var name = ""
        var composition: String? = null
        var proteins: Double? = null
        var fats: Double? = null
        var carbs: Double? = null
        var kcal: Double? = null
        var price: Double? = null
        var url: String? = null
        var category: String? = null
        var fromGreen = false

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
                if (composition.isNullOrBlank() && !n.energyRaw.isNullOrBlank()) {
                    composition = n.energyRaw
                }
            }
        }

        var fromOff = false
        try {
            val resp = off.getProduct(clean)
            if (resp.status == 1 && resp.product != null) {
                fromOff = true
                val prod = resp.product
                val offName = prod.productNameRu?.takeIf { it.isNotBlank() }
                    ?: prod.productName?.takeIf { it.isNotBlank() }
                    ?: prod.genericName.orEmpty()
                if (name.isBlank() && offName.isNotBlank()) name = offName
                val ing = prod.ingredientsTextRu?.takeIf { it.isNotBlank() }
                    ?: prod.ingredientsText
                if (!ing.isNullOrBlank()) composition = ing
                prod.nutriments?.let { nut ->
                    proteins = nut.doubleField("proteins_100g") ?: proteins
                    fats = nut.doubleField("fat_100g") ?: fats
                    carbs = nut.doubleField("carbohydrates_100g") ?: carbs
                    kcal = nut.doubleField("energy-kcal_100g") ?: kcal
                }
            }
        } catch (_: Exception) {
            /* офлайн или ошибка API */
        }

        if (name.isBlank()) {
            name = "Товар не найден"
        }

        return ProductDetail(
            barcode = clean,
            name = name,
            composition = composition,
            proteinsG = proteins,
            fatsG = fats,
            carbsG = carbs,
            kcal100 = kcal,
            priceRub = price,
            greenUrl = url,
            category = category,
            fromGreen = fromGreen,
            fromOpenFoodFacts = fromOff
        )
    }
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
