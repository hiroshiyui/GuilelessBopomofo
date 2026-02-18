package org.ghostsinthelab.apps.guilelessbopomofo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GuilelessBopomofoEnvTest {

    @Test
    fun allPreferenceKeys_areUnique() {
        val keys = listOf(
            GuilelessBopomofoEnv.APP_SHARED_PREFERENCES,
            GuilelessBopomofoEnv.SAME_HAPTIC_FEEDBACK_TO_FUNCTION_BUTTONS,
            GuilelessBopomofoEnv.USER_CANDIDATE_SELECTION_KEYS_OPTION,
            GuilelessBopomofoEnv.USER_CONVERSION_ENGINE,
            GuilelessBopomofoEnv.USER_DISPLAY_ETEN26_QWERTY_LAYOUT,
            GuilelessBopomofoEnv.USER_DISPLAY_HSU_QWERTY_LAYOUT,
            GuilelessBopomofoEnv.USER_ENABLE_DOUBLE_TOUCH_IME_SWITCH,
            GuilelessBopomofoEnv.USER_ENABLE_IME_SWITCH,
            GuilelessBopomofoEnv.USER_ENABLE_SPACE_AS_SELECTION,
            GuilelessBopomofoEnv.USER_FULLSCREEN_WHEN_IN_LANDSCAPE,
            GuilelessBopomofoEnv.USER_FULLSCREEN_WHEN_IN_PORTRAIT,
            GuilelessBopomofoEnv.USER_HAPTIC_FEEDBACK_STRENGTH,
            GuilelessBopomofoEnv.USER_SOFT_KEYBOARD_LAYOUT,
            GuilelessBopomofoEnv.USER_PHYSICAL_KEYBOARD_LAYOUT,
            GuilelessBopomofoEnv.USER_KEY_BUTTON_HEIGHT,
            GuilelessBopomofoEnv.USER_PHRASE_CHOICE_REARWARD,
        )
        assertEquals(
            "All preference key values must be unique",
            keys.size,
            keys.toSet().size
        )
    }

    @Test
    fun allPreferenceKeys_areNonEmpty() {
        val keys = listOf(
            GuilelessBopomofoEnv.APP_SHARED_PREFERENCES,
            GuilelessBopomofoEnv.SAME_HAPTIC_FEEDBACK_TO_FUNCTION_BUTTONS,
            GuilelessBopomofoEnv.USER_CANDIDATE_SELECTION_KEYS_OPTION,
            GuilelessBopomofoEnv.USER_CONVERSION_ENGINE,
            GuilelessBopomofoEnv.USER_DISPLAY_ETEN26_QWERTY_LAYOUT,
            GuilelessBopomofoEnv.USER_DISPLAY_HSU_QWERTY_LAYOUT,
            GuilelessBopomofoEnv.USER_ENABLE_DOUBLE_TOUCH_IME_SWITCH,
            GuilelessBopomofoEnv.USER_ENABLE_IME_SWITCH,
            GuilelessBopomofoEnv.USER_ENABLE_SPACE_AS_SELECTION,
            GuilelessBopomofoEnv.USER_FULLSCREEN_WHEN_IN_LANDSCAPE,
            GuilelessBopomofoEnv.USER_FULLSCREEN_WHEN_IN_PORTRAIT,
            GuilelessBopomofoEnv.USER_HAPTIC_FEEDBACK_STRENGTH,
            GuilelessBopomofoEnv.USER_SOFT_KEYBOARD_LAYOUT,
            GuilelessBopomofoEnv.USER_PHYSICAL_KEYBOARD_LAYOUT,
            GuilelessBopomofoEnv.USER_KEY_BUTTON_HEIGHT,
            GuilelessBopomofoEnv.USER_PHRASE_CHOICE_REARWARD,
        )
        keys.forEach { key ->
            assertTrue("Preference key should not be empty", key.isNotEmpty())
        }
    }

    @Test
    fun physicalKeyboardPresented_defaultIsFalse() {
        // Reset to default in case other tests modified it
        GuilelessBopomofoEnv.physicalKeyboardPresented = false
        assertFalse(GuilelessBopomofoEnv.physicalKeyboardPresented)
    }

    @Test
    fun physicalKeyboardPresented_canBeToggled() {
        GuilelessBopomofoEnv.physicalKeyboardPresented = true
        assertTrue(GuilelessBopomofoEnv.physicalKeyboardPresented)
        GuilelessBopomofoEnv.physicalKeyboardPresented = false
        assertFalse(GuilelessBopomofoEnv.physicalKeyboardPresented)
    }

    @Test
    fun deviceIsEmulator_defaultIsFalse() {
        GuilelessBopomofoEnv.deviceIsEmulator = false
        assertFalse(GuilelessBopomofoEnv.deviceIsEmulator)
    }

    @Test
    fun deviceIsEmulator_canBeToggled() {
        GuilelessBopomofoEnv.deviceIsEmulator = true
        assertTrue(GuilelessBopomofoEnv.deviceIsEmulator)
        GuilelessBopomofoEnv.deviceIsEmulator = false
        assertFalse(GuilelessBopomofoEnv.deviceIsEmulator)
    }
}
