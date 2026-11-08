package org.ghostsinthelab.apps.guilelessbopomofo.ui.ime

import android.view.inputmethod.EditorInfo
import org.ghostsinthelab.apps.guilelessbopomofo.R
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.FunctionKey
import org.ghostsinthelab.apps.guilelessbopomofo.utils.EnterKeyBehavior
import org.junit.Assert.assertEquals
import org.junit.Test

class EnterKeyIconTest {

    @Test
    fun aLineBreak_showsTheReturnArrow() {
        assertEquals(R.drawable.ic_keyboard_return_24, enterKeyIcon(EnterKeyBehavior.NewLine))
    }

    @Test
    fun theCommonActions_haveTheirOwnSymbol() {
        val expected = mapOf(
            EditorInfo.IME_ACTION_SEND to R.drawable.ic_keyboard_action_send_24,
            EditorInfo.IME_ACTION_SEARCH to R.drawable.ic_keyboard_action_search_24,
            EditorInfo.IME_ACTION_GO to R.drawable.ic_keyboard_action_forward_24,
            EditorInfo.IME_ACTION_NEXT to R.drawable.ic_keyboard_action_forward_24,
            EditorInfo.IME_ACTION_PREVIOUS to R.drawable.ic_keyboard_action_back_24,
            EditorInfo.IME_ACTION_DONE to R.drawable.ic_keyboard_action_done_24,
        )
        expected.forEach { (actionId, icon) ->
            assertEquals("action $actionId", icon, enterKeyIcon(EnterKeyBehavior.EditorAction(actionId)))
        }
    }

    @Test
    fun anAppMadeUpActionId_showsADoneMark() {
        assertEquals(R.drawable.ic_keyboard_action_done_24, enterKeyIcon(EnterKeyBehavior.EditorAction(42)))
    }

    @Test
    fun onlyTheEnterKey_changesWithTheTextField() {
        val uiState = ImeUiState().apply {
            enterKeyBehavior = EnterKeyBehavior.EditorAction(EditorInfo.IME_ACTION_SEND)
        }
        assertEquals(R.drawable.ic_keyboard_action_send_24, FunctionKey.ENTER.iconFor(uiState))
        assertEquals(FunctionKey.SPACE.icon, FunctionKey.SPACE.iconFor(uiState))
    }
}
