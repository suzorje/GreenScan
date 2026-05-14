package by.bstu.makovei.greenscan

import by.bstu.makovei.greenscan.data.analysis.DangerLevel
import by.bstu.makovei.greenscan.data.analysis.EAdditiveAnalyzer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EAdditiveAnalyzerTest {

    @Test
    fun `returns empty list for null input`() {
        val result = EAdditiveAnalyzer.analyze(null)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `returns empty list for blank input`() {
        val result = EAdditiveAnalyzer.analyze("   ")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `detects E211 sodium benzoate`() {
        val result = EAdditiveAnalyzer.analyze("Вода, сахар, E211, лимонная кислота")
        assertTrue(result.any { it.code == "E211" })
        assertTrue(result.any { it.dangerLevel == DangerLevel.MODERATE })
    }

    @Test
    fun `detects E250 nitrite sodium as dangerous`() {
        val result = EAdditiveAnalyzer.analyze("Свинина, соль, Е250, специи")
        assertTrue(result.any { it.code == "E250" })
        assertEquals(DangerLevel.DANGEROUS, result.first { it.code == "E250" }.dangerLevel)
    }

    @Test
    fun `detects E300 ascorbic acid as safe`() {
        val result = EAdditiveAnalyzer.analyze("Яблочный сок, E300, соль")
        assertTrue(result.any { it.code == "E300" })
        assertEquals(DangerLevel.SAFE, result.first { it.code == "E300" }.dangerLevel)
    }

    @Test
    fun `detects multiple additives in one string`() {
        val result = EAdditiveAnalyzer.analyze("Мука пшеничная, E471, E202, E330, сахар")
        val codes = result.map { it.code }.toSet()
        assertTrue("E471" in codes)
        assertTrue("E202" in codes)
        assertTrue("E330" in codes)
    }

    @Test
    fun `does not duplicate same additive`() {
        val result = EAdditiveAnalyzer.analyze("E211, вода, E211, сахар")
        assertEquals(1, result.count { it.code == "E211" })
    }

    @Test
    fun `handles Cyrillic E prefix`() {
        val result = EAdditiveAnalyzer.analyze("Е621, глутамат натрия")
        assertTrue(result.any { it.code == "E621" })
    }

    @Test
    fun `detects banned additive E924`() {
        val result = EAdditiveAnalyzer.analyze("Мука, E924, вода")
        assertTrue(result.any { it.code == "E924" })
        assertEquals(DangerLevel.BANNED, result.first { it.code == "E924" }.dangerLevel)
    }
}
