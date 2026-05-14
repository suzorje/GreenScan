package by.bstu.makovei.greenscan.data.analysis

data class Allergen(val key: String, val displayName: String)

object AllergenDetector {

    val ALL_ALLERGENS: List<Allergen> = listOf(
        Allergen("gluten",    "Глютен"),
        Allergen("lactose",   "Лактоза / молоко"),
        Allergen("nuts",      "Орехи"),
        Allergen("peanuts",   "Арахис"),
        Allergen("soy",       "Соя"),
        Allergen("eggs",      "Яйца"),
        Allergen("fish",      "Рыба"),
        Allergen("shellfish", "Моллюски / ракообразные"),
        Allergen("mustard",   "Горчица"),
        Allergen("sesame",    "Кунжут"),
        Allergen("celery",    "Сельдерей"),
        Allergen("lupin",     "Люпин"),
        Allergen("sulfites",  "Сульфиты / диоксид серы")
    )

    private val KEYWORDS: Map<String, List<String>> = mapOf(
        "gluten" to listOf(
            "глютен", "глутен", "пшениц", "пшенич", "рожь", "ржан", "ячмен", "овёс", "овес",
            "полба", "камут", "тритикале", "спельта", "semolina", "wheat", "rye", "barley", "oats", "gluten"
        ),
        "lactose" to listOf(
            "молок", "лактоз", "сыворотк", "сливк", "сливочн", "кефир", "творог",
            "йогурт", "сметан", "казеин", "milk", "lactose", "dairy", "cream", "butter", "cheese", "whey"
        ),
        "nuts" to listOf(
            "грецк", "фундук", "миндаль", "кешью", "пекан", "фисташ", "макадамия",
            "almond", "cashew", "walnut", "hazelnut", "pecan", "pistachio", "macadamia", "brazilnut"
        ),
        "peanuts" to listOf("арахис", "земляной орех", "peanut", "groundnut"),
        "soy"     to listOf("соя", "соев", "soy", "soja", "soybean"),
        "eggs"    to listOf("яйц", "яичн", "желток", "egg", "albumin", "lysozyme"),
        "fish"    to listOf(
            "рыб", "треск", "сёмга", "семга", "лосос", "тунец", "сельд", "скумбри",
            "fish", "cod", "salmon", "tuna", "herring", "mackerel", "anchov"
        ),
        "shellfish" to listOf(
            "краб", "креветк", "омар", "мидии", "устриц", "кальмар", "осьминог",
            "shrimp", "prawn", "lobster", "crab", "oyster", "mussel", "squid", "crustacean", "mollusc"
        ),
        "mustard"  to listOf("горчиц", "mustard", "senf"),
        "sesame"   to listOf("кунжут", "сезам", "sesame", "tahini"),
        "celery"   to listOf("сельдерей", "celery", "celeriac"),
        "lupin"    to listOf("люпин", "lupin", "lupine"),
        "sulfites" to listOf(
            "сульфит", "диоксид серы", "e220", "e221", "e222", "e223", "e224",
            "e226", "e227", "e228", "sulph", "sulfit", "sulfite", "so2"
        )
    )

    fun detect(ingredientsText: String?): List<Allergen> {
        if (ingredientsText.isNullOrBlank()) return emptyList()
        val lower = ingredientsText.lowercase()
        return ALL_ALLERGENS.filter { allergen ->
            KEYWORDS[allergen.key]?.any { keyword -> keyword in lower } == true
        }
    }

    fun detectKeys(ingredientsText: String?): Set<String> =
        detect(ingredientsText).map { it.key }.toSet()
}
