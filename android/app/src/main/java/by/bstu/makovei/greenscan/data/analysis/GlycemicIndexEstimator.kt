package by.bstu.makovei.greenscan.data.analysis

data class GiResult(
    val estimatedGi: Int,
    val category: GiCategory,
    val note: String
)

enum class GiCategory(val label: String) {
    LOW("Низкий ГИ (≤55)"),
    MEDIUM("Средний ГИ (56–69)"),
    HIGH("Высокий ГИ (≥70)")
}

object GlycemicIndexEstimator {

    // Базовые ГИ по категориям продукта (ключевые слова из поля category или names)
    private val CATEGORY_BASE = listOf(
        Pair(listOf("sugar", "сахар", "candy", "конфет", "карамел"), 80),
        Pair(listOf("bread", "хлеб", "батон", "булка", "выпечка", "biscuit", "печень"), 72),
        Pair(listOf("cereal", "хлопья", "мюсли", "granola"), 70),
        Pair(listOf("pasta", "макарон", "noodle"), 55),
        Pair(listOf("rice", "рис"), 65),
        Pair(listOf("potato", "картоф"), 80),
        Pair(listOf("juice", "сок", "nectar", "нектар"), 65),
        Pair(listOf("chocolate", "шоколад"), 40),
        Pair(listOf("dairy", "молоко", "йогурт", "кефир"), 35),
        Pair(listOf("legum", "бобовые", "горох", "чечевица", "фасоль", "bean", "lentil"), 30),
        Pair(listOf("nut", "орех", "nuts"), 15),
        Pair(listOf("fruit", "фрукт", "berr", "ягод", "apple", "яблок", "груш", "pear"), 45),
        Pair(listOf("vegetable", "овощи", "салат", "огурец", "помидор", "tomato"), 25),
        Pair(listOf("meat", "мясо", "chicken", "курица", "beef", "pork", "свинина", "говядина"), 0),
        Pair(listOf("fish", "рыба"), 0),
        Pair(listOf("egg", "яйцо", "яйца"), 0),
        Pair(listOf("oil", "масло", "fat", "жир"), 0),
        Pair(listOf("water", "вода", "drink", "напиток"), 0),
        Pair(listOf("snack", "чипсы", "crisp", "cracke"), 72),
        Pair(listOf("ice cream", "мороженое"), 60),
        Pair(listOf("jam", "варенье", "джем"), 70),
        Pair(listOf("honey", "мёд", "мед"), 61),
        Pair(listOf("cookie", "печенье", "wafer", "вафел"), 70),
        Pair(listOf("cake", "торт", "пирог", "пирожн"), 75)
    )

    fun estimate(
        carbsPer100g: Double?,
        sugarPer100g: Double?,
        fiberPer100g: Double? = null,
        categoryHint: String? = null,
        nameHint: String? = null
    ): GiResult? {
        if (carbsPer100g == null || carbsPer100g < 1.0) return null

        val combinedHint = listOfNotNull(categoryHint, nameHint).joinToString(" ").lowercase()
        var baseGi: Int? = null
        for ((keywords, gi) in CATEGORY_BASE) {
            if (keywords.any { combinedHint.contains(it) }) {
                baseGi = gi
                break
            }
        }

        if (baseGi == null) {
            // Fallback — estimate from sugar/carbs ratio
            val sugarRatio = if (sugarPer100g != null) sugarPer100g / carbsPer100g else 0.3
            baseGi = (40 + sugarRatio * 60).toInt().coerceIn(20, 90)
        }

        // Клетчатка снижает ГИ
        val fiberAdjustment = if (fiberPer100g != null && fiberPer100g > 0) {
            -(fiberPer100g * 2).toInt().coerceAtMost(20)
        } else 0

        val gi = (baseGi + fiberAdjustment).coerceIn(0, 100)

        val category = when {
            gi <= 55 -> GiCategory.LOW
            gi <= 69 -> GiCategory.MEDIUM
            else -> GiCategory.HIGH
        }

        val note = when (category) {
            GiCategory.LOW -> "Медленно повышает уровень глюкозы в крови"
            GiCategory.MEDIUM -> "Умеренно повышает уровень глюкозы"
            GiCategory.HIGH -> "Быстро повышает уровень глюкозы — ограничьте потребление"
        }

        return GiResult(gi, category, note)
    }
}
