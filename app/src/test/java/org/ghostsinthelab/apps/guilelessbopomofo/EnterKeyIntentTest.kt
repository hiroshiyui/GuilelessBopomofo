package org.ghostsinthelab.apps.guilelessbopomofo

import android.view.KeyEvent
import org.ghostsinthelab.apps.guilelessbopomofo.enums.EnterKeyIntent
import org.junit.Assert.assertEquals
import org.junit.Test

class EnterKeyIntentTest {

    @Test
    fun noModifier_leavesItToTheTextField() {
        assertEquals(EnterKeyIntent.EDITOR_ACTION, EnterKeyIntent.forMetaState(0))
    }

    @Test
    fun shift_asksForALineBreak() {
        assertEquals(
            EnterKeyIntent.LINE_BREAK,
            EnterKeyIntent.forMetaState(KeyEvent.META_SHIFT_ON)
        )
    }

    @Test
    fun eitherShiftKey_asksForALineBreak() {
        listOf(KeyEvent.META_SHIFT_LEFT_ON, KeyEvent.META_SHIFT_RIGHT_ON).forEach { metaState ->
            assertEquals(
                "meta state $metaState should ask for a line break",
                EnterKeyIntent.LINE_BREAK,
                EnterKeyIntent.forMetaState(metaState)
            )
        }
    }

    @Test
    fun capsLock_isNotShift() {
        assertEquals(
            EnterKeyIntent.EDITOR_ACTION,
            EnterKeyIntent.forMetaState(KeyEvent.META_CAPS_LOCK_ON)
        )
    }

    @Test
    fun alt_asksForALineBreak() {
        listOf(KeyEvent.META_ALT_ON, KeyEvent.META_ALT_LEFT_ON, KeyEvent.META_ALT_RIGHT_ON).forEach { metaState ->
            assertEquals(
                "meta state $metaState should ask for a line break",
                EnterKeyIntent.LINE_BREAK,
                EnterKeyIntent.forMetaState(metaState)
            )
        }
    }

    @Test
    fun otherModifiers_areNotShiftOrAlt() {
        val metaState = KeyEvent.META_CTRL_ON or KeyEvent.META_SYM_ON or KeyEvent.META_META_ON

        assertEquals(EnterKeyIntent.EDITOR_ACTION, EnterKeyIntent.forMetaState(metaState))
    }

    @Test
    fun shiftAlongsideOtherModifiers_stillAsksForALineBreak() {
        val metaState = KeyEvent.META_SHIFT_ON or KeyEvent.META_CTRL_ON or KeyEvent.META_ALT_ON

        assertEquals(EnterKeyIntent.LINE_BREAK, EnterKeyIntent.forMetaState(metaState))
    }
}
