package org.ghostsinthelab.apps.guilelessbopomofo

import org.junit.Assert.assertEquals
import org.junit.Test

class ChewingEnumsTest {

    // ChiEngMode tests

    @Test
    fun chiEngMode_symbolIsZero() {
        assertEquals(0, ChiEngMode.SYMBOL.mode)
    }

    @Test
    fun chiEngMode_chineseIsOne() {
        assertEquals(1, ChiEngMode.CHINESE.mode)
    }

    @Test
    fun chiEngMode_hasExactlyTwoValues() {
        assertEquals(2, ChiEngMode.entries.size)
    }

    // ShapeMode tests

    @Test
    fun shapeMode_halfIsZero() {
        assertEquals(0, ShapeMode.HALF.mode)
    }

    @Test
    fun shapeMode_fullIsOne() {
        assertEquals(1, ShapeMode.FULL.mode)
    }

    @Test
    fun shapeMode_hasExactlyTwoValues() {
        assertEquals(2, ShapeMode.entries.size)
    }

    // ConversionEngines tests

    @Test
    fun conversionEngines_values() {
        assertEquals(0, ConversionEngines.SIMPLE_CONVERSION_ENGINE.mode)
        assertEquals(1, ConversionEngines.CHEWING_CONVERSION_ENGINE.mode)
        assertEquals(2, ConversionEngines.FUZZY_CHEWING_CONVERSION_ENGINE.mode)
    }

    @Test
    fun conversionEngines_hasExactlyThreeValues() {
        assertEquals(3, ConversionEngines.entries.size)
    }

    // BopomofoSoftKeyboards tests

    @Test
    fun bopomofoSoftKeyboards_hasFiveLayouts() {
        assertEquals(5, BopomofoSoftKeyboards.entries.size)
    }

    @Test
    fun bopomofoSoftKeyboards_allLayoutStringsUnique() {
        val layouts = BopomofoSoftKeyboards.entries.map { it.layout }
        assertEquals(layouts.size, layouts.toSet().size)
    }

    @Test
    fun bopomofoSoftKeyboards_layoutStringsMatchEnumNames() {
        BopomofoSoftKeyboards.entries.forEach { entry ->
            assertEquals(
                "Layout string for ${entry.name} should match its enum name",
                entry.name,
                entry.layout
            )
        }
    }

    // BopomofoPhysicalKeyboards tests

    @Test
    fun bopomofoPhysicalKeyboards_hasSeventeenLayouts() {
        assertEquals(17, BopomofoPhysicalKeyboards.entries.size)
    }

    @Test
    fun bopomofoPhysicalKeyboards_allLayoutStringsUnique() {
        val layouts = BopomofoPhysicalKeyboards.entries.map { it.layout }
        assertEquals(layouts.size, layouts.toSet().size)
    }

    @Test
    fun bopomofoPhysicalKeyboards_layoutStringsMatchEnumNames() {
        BopomofoPhysicalKeyboards.entries.forEach { entry ->
            assertEquals(
                "Layout string for ${entry.name} should match its enum name",
                entry.name,
                entry.layout
            )
        }
    }
}
