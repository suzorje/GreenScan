package by.bstu.makovei.greenscan

import by.bstu.makovei.greenscan.data.analysis.EnergyRawParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.math.abs

class EnergyRawParserTest {

    private fun assertApprox(expected: Double, actual: Double?, label: String) {
        assert(actual != null) { "$label expected $expected, got null" }
        assert(abs(actual!! - expected) < 1.0) { "$label expected $expected, got $actual" }
    }

    @Test
    fun `null input returns all nulls`() {
        val p = EnergyRawParser.parse(null)
        assertNull(p.proteins); assertNull(p.fats); assertNull(p.carbs); assertNull(p.kcal)
    }

    @Test
    fun `standard format with hyphens`() {
        val p = EnergyRawParser.parse(
            "белки - 2,0 г; жиры - 19,0 г; углеводы – 70,0 г; энергетическая ценность –110 ккал/ 460 кДж."
        )
        assertApprox(2.0, p.proteins, "proteins")
        assertApprox(19.0, p.fats, "fats")
        assertApprox(70.0, p.carbs, "carbs")
        assertApprox(110.0, p.kcal, "kcal")
    }

    @Test
    fun `uppercase keywords and comma decimals`() {
        val p = EnergyRawParser.parse("Белки 8,7 г; углеводы 53,5 г; жиры 35 г; Энергетическая ценность, 2368 кДж / 566 ккал")
        assertApprox(8.7, p.proteins, "proteins")
        assertApprox(35.0, p.fats, "fats")
        assertApprox(53.5, p.carbs, "carbs")
        assertApprox(566.0, p.kcal, "kcal")
    }

    @Test
    fun `no spaces between keyword and number`() {
        val p = EnergyRawParser.parse("Белки15 г\nЖиры2 г\nУглеводы60 г\nЭнергетическая ценность на 100 г\n320 ккал.")
        assertApprox(15.0, p.proteins, "proteins")
        assertApprox(2.0, p.fats, "fats")
        assertApprox(60.0, p.carbs, "carbs")
        assertApprox(320.0, p.kcal, "kcal")
    }

    @Test
    fun `comma-field format белки,г-3,5`() {
        val p = EnergyRawParser.parse("белки,г-3,5;жиры,г-10,0;углеводы,г-6,5")
        assertApprox(3.5, p.proteins, "proteins")
        assertApprox(10.0, p.fats, "fats")
        assertApprox(6.5, p.carbs, "carbs")
    }

    @Test
    fun `kJ slash kcal format X kDj slash Y kcal`() {
        val p = EnergyRawParser.parse("908 кДж/ 217 ккал")
        assertApprox(217.0, p.kcal, "kcal")
    }

    @Test
    fun `kJ only fallback converts to kcal`() {
        val p = EnergyRawParser.parse("белки - 18г; жиры-18г;  энергетическая ценность - 938 кДж")
        assertApprox(18.0, p.proteins, "proteins")
        assertApprox(18.0, p.fats, "fats")
        // 938 / 4.184 ≈ 224 kcal
        assertApprox(224.0, p.kcal, "kcal")
    }

    @Test
    fun `X slash Y no unit after ценность`() {
        val p = EnergyRawParser.parse("Белки 8,5, Жиры 11, Углеводы 5, Энергетическая ценность 640/150")
        assertApprox(8.5, p.proteins, "proteins")
        assertApprox(11.0, p.fats, "fats")
        assertApprox(5.0, p.carbs, "carbs")
        assertApprox(150.0, p.kcal, "kcal")  // 640 > 150*3 → 150 is kcal
    }

    @Test
    fun `dot decimal separator`() {
        val p = EnergyRawParser.parse("Белки-7.9г; Жиры-8.8г; Углеводы-77г Энергетическая ценность-419Ккал/1754кДж")
        assertApprox(7.9, p.proteins, "proteins")
        assertApprox(8.8, p.fats, "fats")
        assertApprox(77.0, p.carbs, "carbs")
        assertApprox(419.0, p.kcal, "kcal")
    }

    @Test
    fun `colon separator format`() {
        val p = EnergyRawParser.parse("Белки: 14,49 г\nЖиры: 21,20 г\nУглеводы: 58,00 г\nЭнергетическая ценность: 481,00 ккал")
        assertApprox(14.49, p.proteins, "proteins")
        assertApprox(21.20, p.fats, "fats")
        assertApprox(58.0, p.carbs, "carbs")
        assertApprox(481.0, p.kcal, "kcal")
    }

    @Test
    fun `kcal before kJ with reversed label`() {
        val p = EnergyRawParser.parse("белки – 2,3г, жиры – 0,7г, углеводы – 20,0г., энергетич. ценность - 360кДж/80ккал")
        assertApprox(2.3, p.proteins, "proteins")
        assertApprox(0.7, p.fats, "fats")
        assertApprox(20.0, p.carbs, "carbs")
        assertApprox(80.0, p.kcal, "kcal")
    }

    @Test
    fun `plain kcal label пищевая ценность`() {
        val p = EnergyRawParser.parse("Пищевая ценность 263 ккал\nБелки\t6.1 г\nЖиры\t8.7 г\nУглеводы\t50.5 г")
        assertApprox(263.0, p.kcal, "kcal")
        assertApprox(6.1, p.proteins, "proteins")
        assertApprox(8.7, p.fats, "fats")
        assertApprox(50.5, p.carbs, "carbs")
    }

    @Test
    fun `tab separator between keyword and number`() {
        val p = EnergyRawParser.parse("Белки\t6.1 г\tЖиры\t8.7 г\tУглеводы\t50.5 г")
        assertApprox(6.1, p.proteins, "proteins")
        assertApprox(8.7, p.fats, "fats")
        assertApprox(50.5, p.carbs, "carbs")
    }

    @Test
    fun `all zeros gives zero values not null`() {
        val p = EnergyRawParser.parse("белки - 0,0 г; жиры - 0,0 г; углеводы - 0 г; энергетическая ценность - 0 кДж")
        assertEquals(0.0, p.proteins)
        assertEquals(0.0, p.fats)
        assertEquals(0.0, p.carbs)
    }

    @Test
    fun `garbage string returns nulls`() {
        val p = EnergyRawParser.parse("Температурный режим хранения и транспортирования не ограничен.")
        assertNull(p.proteins)
        assertNull(p.fats)
        assertNull(p.carbs)
        assertNull(p.kcal)
    }
}
