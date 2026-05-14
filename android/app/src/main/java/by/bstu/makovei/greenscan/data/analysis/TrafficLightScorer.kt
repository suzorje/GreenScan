package by.bstu.makovei.greenscan.data.analysis

enum class TrafficColor { GREEN, YELLOW, RED }

data class NutrientScore(
    val label: String,
    val valuePer100g: Double?,
    val color: TrafficColor,
    val unit: String = "г"
)

data class TrafficLightResult(
    val fat: NutrientScore,
    val saturatedFat: NutrientScore,
    val sugar: NutrientScore,
    val salt: NutrientScore,
    val overall: TrafficColor
)

object TrafficLightScorer {

    // Пороги ВОЗ / Роспотребнадзор (г на 100 г продукта)
    private fun colorFat(v: Double?) = when {
        v == null -> TrafficColor.GREEN
        v < 3.0 -> TrafficColor.GREEN
        v <= 17.5 -> TrafficColor.YELLOW
        else -> TrafficColor.RED
    }

    private fun colorSatFat(v: Double?) = when {
        v == null -> TrafficColor.GREEN
        v < 1.5 -> TrafficColor.GREEN
        v <= 5.0 -> TrafficColor.YELLOW
        else -> TrafficColor.RED
    }

    private fun colorSugar(v: Double?) = when {
        v == null -> TrafficColor.GREEN
        v < 5.0 -> TrafficColor.GREEN
        v <= 12.5 -> TrafficColor.YELLOW
        else -> TrafficColor.RED
    }

    private fun colorSalt(v: Double?) = when {
        v == null -> TrafficColor.GREEN
        v < 0.3 -> TrafficColor.GREEN
        v <= 1.5 -> TrafficColor.YELLOW
        else -> TrafficColor.RED
    }

    fun score(
        fat100g: Double?,
        saturatedFat100g: Double?,
        sugar100g: Double?,
        salt100g: Double?,
        sodium100g: Double? = null
    ): TrafficLightResult {
        val effectiveSalt = salt100g ?: sodium100g?.let { it * 2.5 }

        val fatScore = NutrientScore("Жиры", fat100g, colorFat(fat100g))
        val satFatScore = NutrientScore("Насыщ. жиры", saturatedFat100g, colorSatFat(saturatedFat100g))
        val sugarScore = NutrientScore("Сахар", sugar100g, colorSugar(sugar100g))
        val saltScore = NutrientScore("Соль", effectiveSalt, colorSalt(effectiveSalt))

        val colors = listOf(fatScore.color, satFatScore.color, sugarScore.color, saltScore.color)
        val overall = when {
            colors.any { it == TrafficColor.RED } -> TrafficColor.RED
            colors.any { it == TrafficColor.YELLOW } -> TrafficColor.YELLOW
            else -> TrafficColor.GREEN
        }

        return TrafficLightResult(fatScore, satFatScore, sugarScore, saltScore, overall)
    }
}
