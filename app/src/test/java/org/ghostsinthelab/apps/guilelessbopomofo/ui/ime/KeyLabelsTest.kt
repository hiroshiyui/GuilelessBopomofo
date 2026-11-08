package org.ghostsinthelab.apps.guilelessbopomofo.ui.ime

import org.ghostsinthelab.apps.guilelessbopomofo.R
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.BopomofoGlyph
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.CompactDachenKeypad
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.KeySpec
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.KeyboardSpecs
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.KeyboardVariant
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.SoftKeyboard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyLabelsTest {

    /** Drawable ids by name, as the resources would answer `getResourceEntryName`. */
    private val drawableNames: Map<Int, String> =
        R.drawable::class.java.fields.associate { it.getInt(null) to it.name }

    private val bopomofoSymbols = BopomofoGlyph.entries.map { it.symbol }.toSet()

    private fun label(key: KeySpec.Character): String =
        KeyLabels.forCharacterKey(drawableNames.getValue(key.icon), key.keyCode)

    @Test
    fun everyGlyph_hasItsOwnSymbol() {
        assertEquals(BopomofoGlyph.entries.size, bopomofoSymbols.size)
        assertEquals("ㄅ", BopomofoGlyph.B.symbol)
        assertEquals("˙", BopomofoGlyph.TONE_LIGHT.symbol)
    }

    @Test
    fun aKeycapWithSeveralSymbols_readsThemAll() {
        assertEquals("ㄒ ㄕ", KeyLabels.forCharacterKey("guileless_bopomofo_keyboard_hsu_x_shi", "KEYCODE_X"))
        assertEquals("ㄌ ㄥ ㄦ", KeyLabels.forCharacterKey("guileless_bopomofo_keyboard_hsu_l_eng_er", "KEYCODE_L"))
        assertEquals("ㄎ ˋ", KeyLabels.forCharacterKey("guileless_bopomofo_keyboard_dachen_cp26_k_tone4", "KEYCODE_K"))
        assertEquals("ˊ", KeyLabels.forCharacterKey("guileless_bopomofo_keyboard_tone_2", "KEYCODE_6"))
    }

    @Test
    fun aQwertyKeycap_readsTheKeyItself() {
        assertEquals("A", KeyLabels.forCharacterKey("guileless_bopomofo_keyboard_qwerty_a", "KEYCODE_A"))
        assertEquals("`", KeyLabels.forCharacterKey("guileless_bopomofo_keyboard_hsu_qwerty_grave", "KEYCODE_GRAVE"))
        assertEquals("[", KeyLabels.forKeyCode("KEYCODE_LEFT_BRACKET"))
    }

    @Test
    fun anUnknownKeycap_fallsBackToTheKey() {
        assertEquals("Q", KeyLabels.forCharacterKey("some_other_drawable", "KEYCODE_Q"))
    }

    @Test
    fun everyBopomofoKeyboard_labelsEachKeyWithBopomofoSymbols() {
        val qwertyKeycaps = setOf(SoftKeyboard.QWERTY, SoftKeyboard.HSU_QWERTY, SoftKeyboard.ET26_QWERTY)
        (SoftKeyboard.entries - SoftKeyboard.DACHEN_COMPACT - qwertyKeycaps).forEach { keyboard ->
            KeyboardVariant.entries.forEach { variant ->
                val spec = KeyboardSpecs.of(keyboard, variant) ?: return@forEach
                spec.rows.flatten().filterIsInstance<KeySpec.Character>().forEach { key ->
                    val symbols = label(key).split(" ")
                    assertTrue(
                        "$keyboard $variant ${key.keyCode} read as '${label(key)}'",
                        symbols.isNotEmpty() && symbols.all { it in bopomofoSymbols },
                    )
                }
            }
        }
    }

    @Test
    fun everyQwertyKeyboard_labelsEachKeyWithOneCharacter() {
        listOf(SoftKeyboard.QWERTY, SoftKeyboard.HSU_QWERTY, SoftKeyboard.ET26_QWERTY).forEach { keyboard ->
            KeyboardVariant.entries.forEach { variant ->
                val spec = KeyboardSpecs.of(keyboard, variant) ?: return@forEach
                spec.rows.flatten().filterIsInstance<KeySpec.Character>().forEach { key ->
                    assertEquals("$keyboard $variant ${key.keyCode}", 1, label(key).length)
                }
            }
        }
    }

    @Test
    fun aSwipeKey_readsTheTapSymbolFirst() {
        val key = CompactDachenKeypad.rows.flatten().first { it.up != null }
        val label = KeyLabels.forSwipeKey(key)
        assertTrue(label, label.startsWith(key.tap.symbol + " "))
        assertTrue(label, key.up!!.symbol in label)
    }
}
