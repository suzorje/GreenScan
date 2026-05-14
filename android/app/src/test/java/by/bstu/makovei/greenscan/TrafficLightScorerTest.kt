package by.bstu.makovei.greenscan

import by.bstu.makovei.greenscan.data.analysis.TrafficColor
import by.bstu.makovei.greenscan.data.analysis.TrafficLightScorer
import org.junit.Assert.assertEquals
import org.junit.Test

class TrafficLightScorerTest {

    @Test
    fun `all nulls gives green overall`() {
        val result = TrafficLightScorer.score(null, null, null, null)
        assertEquals(TrafficColor.GREEN, result.overall)
    }

    @Test
    fun `low values give green`() {
        val result = TrafficLightScorer.score(
            fat100g = 1.0,
            saturatedFat100g = 0.5,
            sugar100g = 2.0,
            salt100g = 0.1
        )
        assertEquals(TrafficColor.GREEN, result.fat.color)
        assertEquals(TrafficColor.GREEN, result.saturatedFat.color)
        assertEquals(TrafficColor.GREEN, result.sugar.color)
        assertEquals(TrafficColor.GREEN, result.salt.color)
        assertEquals(TrafficColor.GREEN, result.overall)
    }

    @Test
    fun `high fat gives red`() {
        val result = TrafficLightScorer.score(fat100g = 20.0, saturatedFat100g = null, sugar100g = null, salt100g = null)
        assertEquals(TrafficColor.RED, result.fat.color)
        assertEquals(TrafficColor.RED, result.overall)
    }

    @Test
    fun `high sugar gives red`() {
        val result = TrafficLightScorer.score(fat100g = 1.0, saturatedFat100g = null, sugar100g = 15.0, salt100g = null)
        assertEquals(TrafficColor.RED, result.sugar.color)
        assertEquals(TrafficColor.RED, result.overall)
    }

    @Test
    fun `medium salt gives yellow`() {
        val result = TrafficLightScorer.score(fat100g = 1.0, saturatedFat100g = 0.5, sugar100g = 2.0, salt100g = 0.5)
        assertEquals(TrafficColor.YELLOW, result.salt.color)
        assertEquals(TrafficColor.YELLOW, result.overall)
    }

    @Test
    fun `sodium fallback to salt calculation`() {
        // 0.4g sodium * 2.5 = 1.0g salt → YELLOW threshold 0.3–1.5
        val result = TrafficLightScorer.score(fat100g = null, saturatedFat100g = null, sugar100g = null, salt100g = null, sodium100g = 0.4)
        assertEquals(TrafficColor.YELLOW, result.salt.color)
    }

    @Test
    fun `border value fat 3g is yellow`() {
        val result = TrafficLightScorer.score(fat100g = 3.0, saturatedFat100g = null, sugar100g = null, salt100g = null)
        assertEquals(TrafficColor.YELLOW, result.fat.color)
    }

    @Test
    fun `border value sugar 5g is yellow`() {
        val result = TrafficLightScorer.score(fat100g = null, saturatedFat100g = null, sugar100g = 5.0, salt100g = null)
        assertEquals(TrafficColor.YELLOW, result.sugar.color)
    }

    @Test
    fun `mixed scores — worst wins for overall`() {
        val result = TrafficLightScorer.score(
            fat100g = 1.0,       // GREEN
            saturatedFat100g = 2.0, // YELLOW
            sugar100g = 20.0,    // RED
            salt100g = 0.1       // GREEN
        )
        assertEquals(TrafficColor.RED, result.overall)
    }
}
