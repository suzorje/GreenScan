package by.bstu.makovei.greenscan.data.analysis

/**
 * Parses Russian-language free-text nutrition strings from the Green catalogue
 * (nutrition_facts.energy_raw) into structured numeric values.
 *
 * Strategy: lowercase everything, then find each nutrient keyword and greedily skip
 * up to 25 non-digit, non-newline characters before taking the first digit sequence.
 * This handles all separator styles seen in the data (hyphens, colons, comma+unit, tabs, etc.)
 */
object EnergyRawParser {

    data class Parsed(
        val proteins: Double?,
        val fats: Double?,
        val carbs: Double?,
        val kcal: Double?
    )

    private val NUM = """(\d+(?:[,\.]\d+)?)"""

    // Keyword, then skip up to 25 non-digit/non-newline chars (lazy), then number
    private val PROT_RE = Regex("""белк[^\d\r\n]{0,25}?$NUM""")
    private val FAT_RE  = Regex("""жир[^\d\r\n]{0,25}?$NUM""")
    private val CARB_RE = Regex("""углевод[^\d\r\n]{0,25}?$NUM""")

    // Number immediately before "ккал" — most common kcal marker (already lowercased)
    private val KCAL_BEFORE = Regex("""$NUM\s*ккал""")

    // "ценность ... X/Y" or "калорийность ... X/Y" — ratio determines which is kcal
    private val KCAL_SLASH  = Regex("""(?:ценность|калорийность)[^\d]{0,30}?$NUM\s*/\s*$NUM""")

    // kJ only — convert to kcal as last resort (input is already lowercased)
    private val KDJ_RE      = Regex("""$NUM\s*кдж""")

    fun parse(raw: String?): Parsed {
        if (raw.isNullOrBlank()) return Parsed(null, null, null, null)
        val s = raw.lowercase()

        val proteins = PROT_RE.find(s)?.groupValues?.get(1)?.num()
        val fats     = FAT_RE.find(s)?.groupValues?.get(1)?.num()
        val carbs    = CARB_RE.find(s)?.groupValues?.get(1)?.num()
        val kcal     = parseKcal(s)

        return Parsed(proteins, fats, carbs, kcal)
    }

    private fun parseKcal(s: String): Double? {
        // 1. Number directly before "ккал"
        KCAL_BEFORE.find(s)?.groupValues?.get(1)?.num()?.let { return it }

        // 2. X/Y after "ценность":
        //    If X > 3*Y  → X is kJ, Y is kcal
        //    Otherwise   → X is kcal (e.g. "550 ккал/2300кДж" already handled by step 1)
        KCAL_SLASH.find(s)?.let { m ->
            val a = m.groupValues[1].num()
            val b = m.groupValues[2].num()
            if (a != null && b != null) {
                return if (a > b * 3.0) b else a
            }
        }

        // 3. kJ only → convert (÷4.184)
        KDJ_RE.find(s)?.groupValues?.get(1)?.num()?.let { kj ->
            if (kj > 0) return kj / 4.184
        }

        return null
    }

    private fun String.num(): Double? = replace(',', '.').toDoubleOrNull()
}
