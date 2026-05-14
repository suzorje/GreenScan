package by.bstu.makovei.greenscan

import by.bstu.makovei.greenscan.data.analysis.AllergenDetector
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AllergenDetectorTest {

    @Test
    fun `returns empty for null`() {
        assertTrue(AllergenDetector.detect(null).isEmpty())
    }

    @Test
    fun `detects gluten via wheat`() {
        val keys = AllergenDetector.detectKeys("Мука пшеничная, яйцо, масло")
        assertTrue("gluten" in keys)
    }

    @Test
    fun `detects lactose via milk keyword`() {
        val keys = AllergenDetector.detectKeys("Молоко цельное, сахар, ваниль")
        assertTrue("lactose" in keys)
    }

    @Test
    fun `detects soy`() {
        val keys = AllergenDetector.detectKeys("Соевый белок, вода, соль")
        assertTrue("soy" in keys)
    }

    @Test
    fun `detects eggs`() {
        val keys = AllergenDetector.detectKeys("Мука, яйца куриные, молоко")
        assertTrue("eggs" in keys)
    }

    @Test
    fun `detects nuts — almond`() {
        val keys = AllergenDetector.detectKeys("Шоколад, миндаль, какао")
        assertTrue("nuts" in keys)
    }

    @Test
    fun `detects peanuts`() {
        val keys = AllergenDetector.detectKeys("Арахисовая паста, соль, сахар")
        assertTrue("peanuts" in keys)
    }

    @Test
    fun `detects sulfites via E220`() {
        val keys = AllergenDetector.detectKeys("Сок виноградный, E220, сахар")
        assertTrue("sulfites" in keys)
    }

    @Test
    fun `no false positives for plain water`() {
        val keys = AllergenDetector.detectKeys("Вода питьевая очищенная")
        assertTrue(keys.isEmpty())
    }

    @Test
    fun `detects English keywords fish`() {
        val keys = AllergenDetector.detectKeys("Tuna, water, salt, vegetable broth")
        assertTrue("fish" in keys)
    }

    @Test
    fun `multiple allergens at once`() {
        val keys = AllergenDetector.detectKeys("Пшеничная мука, молоко, яйца, соя, грецкий орех")
        assertTrue("gluten" in keys)
        assertTrue("lactose" in keys)
        assertTrue("eggs" in keys)
        assertTrue("soy" in keys)
        assertTrue("nuts" in keys)
    }

    @Test
    fun `does not detect gluten in rice product`() {
        val keys = AllergenDetector.detectKeys("Рис, вода, соль")
        assertFalse("gluten" in keys)
    }
}
