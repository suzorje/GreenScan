package by.bstu.makovei.greenscan.data.analysis

enum class DangerLevel { SAFE, MODERATE, DANGEROUS, BANNED }

data class EAdditive(
    val code: String,
    val name: String,
    val purpose: String,
    val dangerLevel: DangerLevel
)

object EAdditiveAnalyzer {

    private val DICT: Map<String, EAdditive> = mapOf(
        // Красители
        "E100" to EAdditive("E100", "Куркумин", "Краситель", DangerLevel.SAFE),
        "E101" to EAdditive("E101", "Рибофлавин (витамин B2)", "Краситель", DangerLevel.SAFE),
        "E102" to EAdditive("E102", "Тартразин", "Краситель", DangerLevel.MODERATE),
        "E104" to EAdditive("E104", "Хинолиновый жёлтый", "Краситель", DangerLevel.MODERATE),
        "E110" to EAdditive("E110", "Жёлтый солнечный закат FCF", "Краситель", DangerLevel.MODERATE),
        "E120" to EAdditive("E120", "Кармин (кошениль)", "Краситель", DangerLevel.MODERATE),
        "E122" to EAdditive("E122", "Азорубин", "Краситель", DangerLevel.MODERATE),
        "E123" to EAdditive("E123", "Амарант", "Краситель", DangerLevel.DANGEROUS),
        "E124" to EAdditive("E124", "Понсо 4R", "Краситель", DangerLevel.MODERATE),
        "E127" to EAdditive("E127", "Эритрозин", "Краситель", DangerLevel.MODERATE),
        "E129" to EAdditive("E129", "Красный привлекательный AC", "Краситель", DangerLevel.MODERATE),
        "E131" to EAdditive("E131", "Синий патентованный V", "Краститель", DangerLevel.MODERATE),
        "E132" to EAdditive("E132", "Индигокармин", "Краситель", DangerLevel.MODERATE),
        "E133" to EAdditive("E133", "Синий блестящий FCF", "Краситель", DangerLevel.MODERATE),
        "E140" to EAdditive("E140", "Хлорофилл", "Краситель", DangerLevel.SAFE),
        "E150a" to EAdditive("E150a", "Сахарный колер I", "Краситель", DangerLevel.SAFE),
        "E150b" to EAdditive("E150b", "Сахарный колер II", "Краситель", DangerLevel.MODERATE),
        "E150c" to EAdditive("E150c", "Сахарный колер III (аммиачный)", "Краситель", DangerLevel.MODERATE),
        "E150d" to EAdditive("E150d", "Сахарный колер IV", "Краситель", DangerLevel.MODERATE),
        "E151" to EAdditive("E151", "Чёрный блестящий BN", "Краситель", DangerLevel.MODERATE),
        "E160a" to EAdditive("E160a", "Бета-каротин", "Краситель", DangerLevel.SAFE),
        "E160b" to EAdditive("E160b", "Аннато", "Краситель", DangerLevel.SAFE),
        "E171" to EAdditive("E171", "Диоксид титана", "Краситель", DangerLevel.DANGEROUS),
        "E172" to EAdditive("E172", "Оксиды железа", "Краситель", DangerLevel.SAFE),
        // Консерванты
        "E200" to EAdditive("E200", "Сорбиновая кислота", "Консервант", DangerLevel.SAFE),
        "E202" to EAdditive("E202", "Сорбат калия", "Консервант", DangerLevel.SAFE),
        "E210" to EAdditive("E210", "Бензойная кислота", "Консервант", DangerLevel.MODERATE),
        "E211" to EAdditive("E211", "Бензоат натрия", "Консервант", DangerLevel.MODERATE),
        "E212" to EAdditive("E212", "Бензоат калия", "Консервант", DangerLevel.MODERATE),
        "E213" to EAdditive("E213", "Бензоат кальция", "Консервант", DangerLevel.MODERATE),
        "E216" to EAdditive("E216", "Пропилпарабен", "Консервант", DangerLevel.DANGEROUS),
        "E220" to EAdditive("E220", "Диоксид серы", "Консервант/антиоксидант", DangerLevel.MODERATE),
        "E221" to EAdditive("E221", "Сульфит натрия", "Консервант", DangerLevel.MODERATE),
        "E249" to EAdditive("E249", "Нитрит калия", "Консервант", DangerLevel.DANGEROUS),
        "E250" to EAdditive("E250", "Нитрит натрия", "Консервант", DangerLevel.DANGEROUS),
        "E251" to EAdditive("E251", "Нитрат натрия", "Консервант", DangerLevel.MODERATE),
        "E252" to EAdditive("E252", "Нитрат калия", "Консервант", DangerLevel.MODERATE),
        "E270" to EAdditive("E270", "Молочная кислота", "Консервант/регулятор кислотности", DangerLevel.SAFE),
        "E280" to EAdditive("E280", "Пропионовая кислота", "Консервант", DangerLevel.SAFE),
        "E282" to EAdditive("E282", "Пропионат кальция", "Консервант", DangerLevel.SAFE),
        "E284" to EAdditive("E284", "Борная кислота", "Консервант", DangerLevel.BANNED),
        "E285" to EAdditive("E285", "Тетраборат натрия (бура)", "Консервант", DangerLevel.BANNED),
        // Антиоксиданты
        "E300" to EAdditive("E300", "Аскорбиновая кислота (витамин C)", "Антиоксидант", DangerLevel.SAFE),
        "E301" to EAdditive("E301", "Аскорбат натрия", "Антиоксидант", DangerLevel.SAFE),
        "E306" to EAdditive("E306", "Токоферол (витамин E)", "Антиоксидант", DangerLevel.SAFE),
        "E310" to EAdditive("E310", "Пропилгаллат", "Антиоксидант", DangerLevel.MODERATE),
        "E320" to EAdditive("E320", "Бутилгидроксианизол (БГА)", "Антиоксидант", DangerLevel.DANGEROUS),
        "E321" to EAdditive("E321", "Бутилгидрокситолуол (БГТ)", "Антиоксидант", DangerLevel.MODERATE),
        // Регуляторы кислотности / стабилизаторы
        "E330" to EAdditive("E330", "Лимонная кислота", "Регулятор кислотности", DangerLevel.SAFE),
        "E331" to EAdditive("E331", "Цитрат натрия", "Регулятор кислотности", DangerLevel.SAFE),
        "E332" to EAdditive("E332", "Цитрат калия", "Регулятор кислотности", DangerLevel.SAFE),
        "E338" to EAdditive("E338", "Фосфорная кислота", "Регулятор кислотности", DangerLevel.MODERATE),
        "E339" to EAdditive("E339", "Фосфаты натрия", "Регулятор кислотности", DangerLevel.MODERATE),
        "E340" to EAdditive("E340", "Фосфаты калия", "Регулятор кислотности", DangerLevel.MODERATE),
        "E341" to EAdditive("E341", "Фосфаты кальция", "Регулятор кислотности", DangerLevel.SAFE),
        // Эмульгаторы / стабилизаторы
        "E400" to EAdditive("E400", "Альгиновая кислота", "Загуститель", DangerLevel.SAFE),
        "E401" to EAdditive("E401", "Альгинат натрия", "Загуститель", DangerLevel.SAFE),
        "E406" to EAdditive("E406", "Агар", "Загуститель", DangerLevel.SAFE),
        "E407" to EAdditive("E407", "Каррагинан", "Загуститель", DangerLevel.MODERATE),
        "E412" to EAdditive("E412", "Гуаровая камедь", "Загуститель", DangerLevel.SAFE),
        "E415" to EAdditive("E415", "Ксантановая камедь", "Загуститель", DangerLevel.SAFE),
        "E420" to EAdditive("E420", "Сорбит", "Подсластитель/увлажнитель", DangerLevel.SAFE),
        "E422" to EAdditive("E422", "Глицерин", "Влагоудержатель", DangerLevel.SAFE),
        "E432" to EAdditive("E432", "Полисорбат 20", "Эмульгатор", DangerLevel.MODERATE),
        "E433" to EAdditive("E433", "Полисорбат 80", "Эмульгатор", DangerLevel.MODERATE),
        "E442" to EAdditive("E442", "Аммонийные фосфатиды", "Эмульгатор", DangerLevel.SAFE),
        "E450" to EAdditive("E450", "Дифосфаты", "Разрыхлитель/эмульгатор", DangerLevel.MODERATE),
        "E451" to EAdditive("E451", "Трифосфаты", "Разрыхлитель", DangerLevel.MODERATE),
        "E452" to EAdditive("E452", "Полифосфаты", "Эмульгатор", DangerLevel.MODERATE),
        "E471" to EAdditive("E471", "Моно- и диглицериды жирных кислот", "Эмульгатор", DangerLevel.SAFE),
        "E472e" to EAdditive("E472e", "Моно-диацетилвинная кислота", "Эмульгатор", DangerLevel.SAFE),
        "E476" to EAdditive("E476", "Полиглицерин-полирицинолеат", "Эмульгатор", DangerLevel.SAFE),
        "E481" to EAdditive("E481", "Стеароиллактилат натрия", "Эмульгатор", DangerLevel.SAFE),
        // Усилители вкуса
        "E620" to EAdditive("E620", "Глутаминовая кислота", "Усилитель вкуса", DangerLevel.MODERATE),
        "E621" to EAdditive("E621", "Глутамат натрия (MSG)", "Усилитель вкуса", DangerLevel.MODERATE),
        "E627" to EAdditive("E627", "Гуанилат натрия", "Усилитель вкуса", DangerLevel.MODERATE),
        "E631" to EAdditive("E631", "Инозинат натрия", "Усилитель вкуса", DangerLevel.MODERATE),
        "E635" to EAdditive("E635", "Рибонуклеотиды натрия", "Усилитель вкуса", DangerLevel.MODERATE),
        // Подсластители
        "E950" to EAdditive("E950", "Ацесульфам калия", "Подсластитель", DangerLevel.MODERATE),
        "E951" to EAdditive("E951", "Аспартам", "Подсластитель", DangerLevel.MODERATE),
        "E952" to EAdditive("E952", "Цикламаты", "Подсластитель", DangerLevel.DANGEROUS),
        "E954" to EAdditive("E954", "Сахарин", "Подсластитель", DangerLevel.MODERATE),
        "E955" to EAdditive("E955", "Сукралоза", "Подсластитель", DangerLevel.SAFE),
        "E960" to EAdditive("E960", "Стевиол-гликозиды", "Подсластитель", DangerLevel.SAFE),
        "E961" to EAdditive("E961", "Неотам", "Подсластитель", DangerLevel.SAFE),
        // Прочее
        "E500" to EAdditive("E500", "Карбонаты натрия", "Разрыхлитель", DangerLevel.SAFE),
        "E503" to EAdditive("E503", "Карбонаты аммония", "Разрыхлитель", DangerLevel.SAFE),
        "E507" to EAdditive("E507", "Соляная кислота", "Регулятор кислотности", DangerLevel.SAFE),
        "E551" to EAdditive("E551", "Диоксид кремния", "Противослёживающий агент", DangerLevel.SAFE),
        "E553b" to EAdditive("E553b", "Тальк", "Противослёживающий агент", DangerLevel.MODERATE),
        "E901" to EAdditive("E901", "Пчелиный воск", "Глазирователь", DangerLevel.SAFE),
        "E903" to EAdditive("E903", "Карнаубский воск", "Глазирователь", DangerLevel.SAFE),
        "E904" to EAdditive("E904", "Шеллак", "Глазирователь", DangerLevel.SAFE),
        "E924" to EAdditive("E924", "Бромат калия", "Улучшитель муки", DangerLevel.BANNED),
        "E926" to EAdditive("E926", "Диоксид хлора", "Улучшитель муки/дезинфектант", DangerLevel.BANNED)
    )

    private val CODE_REGEX =
        Regex("""[EЕ](\d{3,4}[a-z]?(?:[ivx]{1,4})?)""", RegexOption.IGNORE_CASE)

    fun analyze(ingredientsText: String?): List<EAdditive> {
        if (ingredientsText.isNullOrBlank()) return emptyList()
        val found = mutableSetOf<String>()
        val result = mutableListOf<EAdditive>()
        for (match in CODE_REGEX.findAll(ingredientsText)) {
            val rawCode = match.value.uppercase().replace("Е", "E")
            if (found.add(rawCode)) {
                val entry = DICT[rawCode]
                    ?: DICT[rawCode.replace(Regex("[IVXABC]+$"), "")]
                result.add(entry ?: EAdditive(rawCode, rawCode, "Неизвестная добавка", DangerLevel.MODERATE))
            }
        }
        return result
    }
}
